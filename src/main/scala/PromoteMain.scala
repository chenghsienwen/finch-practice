import java.util.UUID

import cats.effect.IO
import com.twitter.app.Flag
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.stats.Counter
import com.twitter.finagle.{Http, Service}
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import domain.{ClientId, SessionId, TTL}
import domain.db.{Round, RoundNotFound, dbResult}
import domain.http.CreateVendingRequest
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import io.finch.todo.TodoMain.{adminHttpServer, jsonBody, port, post, statsReceiver, todos}
import util.EnvUtils._
import util.AppConfigLib.getConfig
import domain.http._
import util.PipeOperator.Pipe
import util.IdUtils
import util.TimestampUtils
import domain.errors.Errors._
import domain.errors._
import cats.syntax.option._
import io.finch.todo.Todo
import shapeless._
import shapeless.syntax.typeable._

/**
  * A simple Finch application implementing the backend for the Promote project.
  *
  * Use the following HTTPie commands to test endpoints.
  *
  */
object PromoteMain extends TwitterServer with Endpoint.Module[IO] {

  val port = flag("port", getConfig[String]("FINCH_HTTP_PORT").fold("8888")(x => s"$x"), "TCP port for HTTP server")

  val adminAccountIds     = env[Array[String]](ADMIN_ACCOUNT_ID_WHITELIST_ENV).getOrElse(Array.empty[String])
  val sessionTTLSecs      = env[Long](REDIS_TTL_SECONDS_ENV).getOrElse(30 * 86400L)
  private val idUtil      = new IdUtils()
  private val timeUtil    = new TimestampUtils()
  private val promoteRepo = new PromoteRepo(idUtil, timeUtil)
  def CheckRole(accountId: ClientId): IO[CreateVendingResult] =
    (accountId.v.isEmpty match {
      case true => CreateVendingNotFound()
      case false => {
        (adminAccountIds.contains(accountId.v) match {
          case true  ⇒ CreateVendingAllow()
          case false ⇒ CreateVendingNotAllow().$$(_ ⇒ s"Account id : ${accountId.v} isn't allowed")
        })
      }
    }) |> (IO(_))

  def createVending: Endpoint[IO, CreateVendingSucceed] =
    post(
      "api" :: "mwc_promote" :: "v1" :: "admin" :: "game" :: "register_vending" :: header("X-HTC-Account-Id") :: jsonBody[
        CreateVendingRequest
      ]
    ) { (header: String, req: CreateVendingRequest) =>
      (for {
        isAllow <- CheckRole(header |> ClientId)
        res     <- promoteRepo.insertVending(req)
      } yield {
        (isAllow, res)
      }).map {
        case (a: CreateVendingAllow, r: CreateVendingSucceed) => Ok(r)
        case (e: CreateVendingNotAllow, _)                    => Unauthorized(AccountIdNotAllowed)
        case (e: CreateVendingNotFound, _)                    => BadRequest(AccountIdNotFound)
        case (_, _)                                           => InternalServerError(CreateVendingInternalError)
      }
    }

  def createSession: Endpoint[IO, CreateSessionResponse] =
    post("api" :: "mwc_promote" :: "v1" :: "game" :: "create" :: jsonBody[CreateSessionRequest]) {
      (req: CreateSessionRequest) =>
        (for {
          isAllow <- promoteRepo.isVendingOrAdmin(req.clientId |> ClientId)
          res     <- promoteRepo.insertSession(CreateSessionCacheRequest(sessionTTLSecs, req.clientId.some))
        } yield {
          (isAllow, res)
        }).map {
          case (a: Boolean, r: Round) if a == true => Ok(r._id |> CreateSessionResponse)
          case (a: Boolean, _) if a == false       => Unauthorized(ClientIdNotAllowed)
          case (_, _)                              => InternalServerError(CreateSessionInternalError)
        }
    }

  val provideRound: (SessionId, Option[ClientId], UpdateSessionRequest) => Round = { (sessionId, clientId, req) =>
    Round(
      _id = sessionId.v,
      clientId = clientId.map(_.v),
      status = req.status,
      handsetItem = req.handsetItem,
      imageUrl = req.imageUrl,
      createTime = timeUtil.currentTimestamp(),
      updateTime = timeUtil.currentTimestamp()
    )
  }

  val validateUpdateSession: (Round, ClientId) => IO[UpdateSessionResult] = { (session, clientId) =>
    //is vending
    //is matched clientId
    //get valid session info
    promoteRepo.isVendingOrAdmin(clientId).map {
      case true => UpdateSessionAllow()
      case false =>
        session.clientId.map(i => i.equalsIgnoreCase(clientId.v)) match {
          case Some(true)  => UpdateSessionAllow()
          case Some(false) => UpdateSessionNotAllow()
          case None        => UpdateSessionAllow()
        }

    }
  }

  def updateSession: Endpoint[IO, UpdateSessionResponse] =
    patch(
      "api" :: "mwc_promote" :: "v1" :: "game" :: path[String]
        .withToString("sessionId") :: param[String]("clientId") :: jsonBody[UpdateSessionRequest]
    ) { (sessionId: String, clientId: String, body: UpdateSessionRequest) =>
      (for {
        sessionResult <- promoteRepo.getSession(sessionId |> SessionId)
        isAllow       <- validateUpdateSession(sessionResult.cast[Round].getOrElse(Round()), clientId |> ClientId)
        res <- promoteRepo.updateSession(
                sessionResult.cast[Round].getOrElse(Round()),
                provideRound(sessionId |> SessionId, Some(clientId |> ClientId), body),
                Some(sessionTTLSecs |> TTL)
              )
      } yield {
        (sessionResult, isAllow, res)
      }).map {
        case (item: Round, a: UpdateSessionAllow, r: UpdateSessionResponse) => Ok(r)
        case (item: Round, a: UpdateSessionNotAllow, _)                     => Unauthorized(AccountIdNotAllowed)
        case (item: RoundNotFound, _, _)                                    => BadRequest(AccountIdNotFound)
        case (_, _, _)                                                      => InternalServerError(UpdateSessionInternalError)
      }

      Ok(Round() |> UpdateSessionResponse)
    }

  val getSessionImpl: (SessionId, ClientId) => IO[(dbResult, GetSessionResult)] = { (sessionId, clientId) =>
    for {
      session <- promoteRepo.getSession(sessionId)
      isAllow <- validateGetSession(session.cast[Round].getOrElse(Round()), clientId)
    } yield {
      (session, isAllow)
    }
  }

  val validateGetSession: (Round, ClientId) => IO[GetSessionResult] = { (session, reqClientId) =>
    //is vending
    //is matched clientId
    //get valid session info
    promoteRepo.isVendingOrAdmin(reqClientId).map {
      case true => GetSessionAllow()
      case false =>
        session.clientId.map(i => i.equalsIgnoreCase(reqClientId.v)) match {
          case Some(true)  => GetSessionAllow()
          case Some(false) => GetSessionNotAllow()
          case None        => GetSessionNotFound()
        }
    }
  }

  def getSession: Endpoint[IO, GetSessionResponse] =
    get("api" :: "mwc_promote" :: "v1" :: "game" :: path[String].withToString("sessionId") :: param[String]("clientId")) {
      (sessionId: String, clientId: String) =>
        getSessionImpl(sessionId |> SessionId, clientId |> ClientId).map {
          case (i: Round, r: GetSessionAllow)    => Ok(i |> GetSessionResponse)
          case (i: Round, r: GetSessionNotAllow) => Unauthorized(ClientIdNotAllowed)
          case (i: RoundNotFound, _)             => BadRequest(AccountIdNotFound)
        }
    }

  val api: Service[Request, Response] = (
    createVending :+: createSession :+: updateSession :+: getSession
  ).handle({
      case e: Exception => InternalServerError(e)
    })
    .toServiceAs[Application.Json]

  def main(): Unit = {
    println("Serving the promote API") //scalastyle:ignore
    println(s"port ${port()}")
    println(s"api status ${api.status}")
    val server = Http.server
      .withStatsReceiver(statsReceiver)
      .serve(s":${port()}", api)

    onExit { server.close() }

    Await.ready(adminHttpServer)
  }
}
