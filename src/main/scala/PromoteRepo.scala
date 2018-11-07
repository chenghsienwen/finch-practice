import cats.effect.IO
import cats.syntax.option._
import com.twitter.util.logging.Logging
import domain.db._
import domain.errors.Errors._
import domain.http._
import domain.{ClientId, SessionId, TTL}
import modules.RedisCacheModule
//import scalacache.CatsEffect.modes._
import scalacache.modes.scalaFuture._
import util.EnvUtils._
import util.PipeOperator._
import util.{IdUtils, TimestampUtils}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

trait RepoResult

final case class CreateSessionCacheRequest(ttl: Long, clientIdOpt: Option[String])
final case class deleteSessionResult(v: String) extends RepoResult
final case class deleteVendingResult() extends RepoResult

class PromoteRepo (idUtils: IdUtils, timestampUtils: TimestampUtils) extends Logging{
  val redisVendingCache = RedisCacheModule.redisVendingCache
  val redisSessionCache = RedisCacheModule.redisSessionCache
  val adminAccountIds = env[Array[String]](ADMIN_ACCOUNT_ID_WHITELIST_ENV).getOrElse(Array.empty[String])

  def insertVending: (CreateVendingRequest) => IO[CreateVendingResult] = { request =>
    val result = redisVendingCache
      .get(VENDING_IDS)
      .map {
          case Some(r: VendingMachine) => (request.vendingIds ++ r.vendingIdList).distinct
          case None                    => request.vendingIds
      }
      .map { ids =>
        val result = VendingMachine(ids, timestampUtils.currentTimestamp())
        redisVendingCache.put(VENDING_IDS)(result)
        ids |> CreateVendingSucceed
      }
      .recover {
        case t =>
          error("Insert vendingIds Fail", t)
          throw CreateVendingInternalError
      }
    result |> (IO(_)) |> IO.fromFuture
  }

  def insertSession: (CreateSessionCacheRequest) => IO[Round] = { request =>
    val round = Round(
      _id = idUtils.generateSessionId(),
      clientId = request.clientIdOpt,
      createTime = timestampUtils.currentTimestamp(),
      updateTime = timestampUtils.currentTimestamp()
    )
    redisSessionCache.put(round._id)(round, ttl = (request.ttl).seconds.some)
    round |> (IO(_))
  }

  def updateSession: (Round, Round, Option[TTL]) => IO[UpdateSessionResponse] = { (round, req, ttl) =>
    val updateRound = round.copy(
      clientId = round.clientId.orElse(req.clientId),
      status = req.status.orElse(round.status),
      handsetItem = req.handsetItem.orElse(round.handsetItem),
      imageUrl = req.imageUrl.orElse(round.imageUrl),
      updateTime = timestampUtils.currentTimestamp()
    )
    val expire =
      ttl.map(t => Math.max(t.v - ((timestampUtils.currentTimestamp() - round.createTime) / 1000), 1).seconds)

    redisSessionCache.put(round._id)(updateRound, ttl = expire.#!("ttl =>"))
    updateRound |> UpdateSessionResponse |> (IO(_))
  }

  def getSession: (SessionId) => IO[dbResult] = { request =>
    redisSessionCache
      .get(request.v)
      .map {
        case Some(r) => r
        case None    => RoundNotFound()
      }
      .recover {
        case t =>
          error("Get session Fail", t)
          RoundInternalError()
      } |> (IO(_)) |> IO.fromFuture
  }

  def getVending: () => IO[VendingMachine] = { () =>
    redisVendingCache
      .get(VENDING_IDS)
      .map(i => i.getOrElse(VendingMachine(List.empty[String], 0L)))
      .recover {
        case t =>
          error("Get vendingIds Fail", t)
          throw GetVendingIdInternalError
      } |> (IO(_)) |> IO.fromFuture
  }


  def isVendingOrAdmin(id: ClientId): IO[Boolean] =
    for {
      list <- getVending()
    } yield {
      list.vendingIdList.contains(id.v) | adminAccountIds.contains(id.v)
    }

  def deleteSession: (SessionId) => IO[RepoResult] = { request =>
    redisSessionCache.remove(request.v)
    request.v |> deleteSessionResult |> (IO(_))
  }
  def deleteVending: () => IO[RepoResult] = { () =>
    redisSessionCache.remove(VENDING_IDS)
    deleteVendingResult() |> (IO(_))
  }

}
