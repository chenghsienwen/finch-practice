package modules

import com.google.inject.Provides
import domain.db.{Config, Round, VendingMachine}
import javax.inject.Singleton
import modules.annotations.{RedisConfigCache, RedisSessionCache, RedisVendingCache}
import scalacache.Cache
import scalacache.redis.RedisCache
import scalacache.serialization.binary._
import util.EnvUtils._

object RedisCacheModule {

  case class HostInfo(host: String, port: Int)

  val DEFAULT_REDIS_PORT = 6379
  val redisHost = env[String](REDIS_HOST_ENV).getOrElse("localhost:6379")
    //flag[String](REDIS_HOST_FLAG, env[String](REDIS_HOST_ENV).getOrElse("localhost:6379"), "redis host name")

  @Singleton
  @Provides
  @RedisVendingCache
  def redisVendingCache: Cache[VendingMachine] = RedisCache(getHostInfo().host, getHostInfo().port)

  @Singleton
  @Provides
  @RedisSessionCache
  def redisSessionCache: Cache[Round] = RedisCache(getHostInfo().host, getHostInfo().port)

  @Singleton
  @Provides
  @RedisConfigCache
  def redisConfigCache: Cache[Config] = RedisCache(getHostInfo().host, getHostInfo().port)

  /**
    * get redis host and port from redis host flag
    * @return
    */
  def getHostInfo(): HostInfo = {
    val hostInfo = redisHost.split(":").toList
    val host     = hostInfo.headOption.getOrElse("localhost")

    hostInfo.size == 2 match {
      case true  => HostInfo(host, hostInfo.lastOption.map(_.toInt).getOrElse(DEFAULT_REDIS_PORT))
      case false => HostInfo(host, DEFAULT_REDIS_PORT)
    }
  }
}
