import cats.effect.IO
import cats.syntax.option._
import com.twitter.util.logging.Logging
import domain.db.{Round, VendingMachine}
import domain.errors.{Errors, RepoResult}
import domain.http._
import domain.{SessionId, TTL}
import modules.RedisCacheModule
import scalacache.modes.scalaFuture._
import util.EnvUtils._
import util.PipeOperator._
import util.{IdUtils, TimestampUtils}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

final case class CreateSessionCacheRequest(ttl: Long, clientIdOpt: Option[String])
final case class deleteSessionResult(v: String) extends RepoResult
final case class deleteVendingResult() extends RepoResult

class promoteRepo (idUtils: IdUtils, timestampUtils: TimestampUtils) extends Logging{
  val redisVendingCache = RedisCacheModule.redisVendingCache
  val redisSessionCache = RedisCacheModule.redisSessionCache
  val sessionTTLSecs = env[Int](REDIS_TTL_SECONDS_ENV).getOrElse(30 * 86400)

  def insertVending: (CreateVendingRequest) => IO[RepoResult] = { request =>
    val result = redisVendingCache
      .get(VENDING_IDS)
      .map {
          case Some(r: VendingMachine) => (request.vendingIds ++ r.vendingIdList).distinct
          case None                    => request.vendingIds
      }
      .map { ids =>
        val result = VendingMachine(ids, timestampUtils.currentTimestamp())
        redisVendingCache.put(VENDING_IDS)(result)
        ids |> CreateVendingResponse
      }
      .recover {
        case t =>
          error("Insert vendingIds Fail", t)
          Errors.CreateVendingInternalError
      }
    result |> (IO(_)) |> IO.fromFuture
  }

  def insertSession: (CreateSessionCacheRequest) => IO[RepoResult] = { request =>
    val round = Round(
      _id = idUtils.generateSessionId(),
      clientId = request.clientIdOpt,
      createTime = timestampUtils.currentTimestamp(),
      updateTime = timestampUtils.currentTimestamp()
    )
    redisSessionCache.put(round._id)(round, ttl = (request.ttl).seconds.some)
    round |> (IO(_))
  }

  def updateSession: (Round, Round, Option[TTL]) => IO[RepoResult] = { (round, req, ttl) =>
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
    updateRound |> (IO(_))
  }

  def getSession: (SessionId) => IO[RepoResult] = { request =>
    redisSessionCache
      .get(request.v)
      .map {
        case Some(r) => r
        case None    => Errors.SessionNotFound
      }
      .recover {
        case t =>
          error("Get session Fail", t)
          Errors.GetSessionIdInternalError
      } |> (IO(_)) |> IO.fromFuture
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
