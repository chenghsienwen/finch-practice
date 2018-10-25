import java.util.UUID

import cats.effect.IO
import com.twitter.app.Flag
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.stats.Counter
import com.twitter.finagle.{Http, Service}
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import domain.db.Round
import domain.http.CreateVendingRequest
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import io.finch.todo.TodoMain.{adminHttpServer, statsReceiver}
import util.EnvUtils._
import util.AppConfigLib.getConfig
import domain.http._
import util.PipeOperator.Pipe

/**
  * A simple Finch application implementing the backend for the Promote project.
  *
  * Use the following HTTPie commands to test endpoints.
  *
  */
object PromoteMain extends TwitterServer with Endpoint.Module[IO] {

  val port = flag("port", getConfig[String]("FINCH_HTTP_PORT").fold(":8888")(x => s":$x"), "TCP port for HTTP server")

  def createVending: Endpoint[IO, CreateVendingResponse] =
    post(
      "/api/mwc_promote/v1/admin/game/register_vending" :: header("X-HTC-Account-Id") :: jsonBody[CreateVendingRequest]
    ) { (header: String, req: CreateVendingRequest) =>
      Ok(req.vendingIds |> CreateVendingResponse)
    }

  def createSession: Endpoint[IO, CreateSessionResponse] = post("/api/mwc_promote/v1/game/create" :: jsonBody[CreateSessionRequest]) {
    req: CreateSessionRequest => Ok(req.clientId |> CreateSessionResponse)
  }

  def updateSession: Endpoint[IO, UpdateSessionResponse] = patch("/api/mwc_promote/v1/game/" :: path[String].withToString("sessionId") :: param[String]("clientId") :: jsonBody[UpdateSessionRequest]) {
    (sessionId: String, clientId: String, body: UpdateSessionRequest) =>
      Ok(Round() |> UpdateSessionResponse)
  }

  def getSession: Endpoint[IO, GetSessionResponse] = get("/api/mwc_promote/v1/game/" :: path[String].withToString("sessionId") :: param[String]("clientId")) {
    (sessionId: String, clientId: String) =>
      Ok(Round() |> GetSessionResponse)
  }

  def deleteSession: Endpoint[IO, DeleteSessionResponse] = delete("/api/mwc_promote/v1/admin/game/delete" :: header("X-HTC-Account-Id") :: params[String]("sessionId")) {
    (header: String, sessionIds: Seq[String]) =>
      Ok(sessionIds.toList |> DeleteSessionResponse)
  }

  val api: Service[Request, Response] = (
    createVending :+: createSession :+: updateSession :+: getSession :+: deleteSession
  ).toServiceAs[Application.Json]

  def main(): Unit = {
    println("Serving the promote API") //scalastyle:ignore

    val server = Http.server
      .withStatsReceiver(statsReceiver)
      .serve(port(), api)

    onExit { server.close() }

    Await.ready(adminHttpServer)
  }
}
