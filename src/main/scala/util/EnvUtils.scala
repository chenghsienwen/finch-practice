package util

import scala.util.control.Exception._
//import ciris.{env}
import cats.syntax.option._
object EnvUtils {

  implicit val optionLongConverter  = new LongConverter()
  implicit val intConverter         = new IntConverter()
  implicit val stringConverter      = new StringConverter()
  implicit val stringListConverter  = new StringListConverter()
  implicit val stringArrayConverter = new StringArrayConverter()

  val ADMIN_ACCOUNT_ID_WHITELIST_ENV    = "ADMIN_ACCOUNT_ID_WHITELIST"
  val REDIS_HOST_ENV                    = "REDIS_HOST"
  val REDIS_TTL_SECONDS_ENV             = "REDIS_TTL_SECONDS"
  val REDIS_KEY_PREFIX_ENV              = "REDIS_KEY_PREFIX"

  val redisKeyPrefix = env[String](REDIS_KEY_PREFIX_ENV).getOrElse("vad")
  val VENDING_IDS    = s"${redisKeyPrefix}-VendingIds"
  val CONFIG         = s"${redisKeyPrefix}-Config"

  def env[T](key: String)(implicit convert: ValueConverter[T]): Option[T] =
    sys.env.get(key).filter(_.nonEmpty).map(convert.convert)
}

trait ValueConverter[T] {
  def convert(v: String): T
}

class OptionIntConverter extends ValueConverter[Option[Int]] {
  override def convert(v: String): Option[Int] = allCatch.opt(v.toInt)
}

class LongConverter extends ValueConverter[Long] {
  override def convert(v: String): Long = v.toLong
}

class IntConverter extends ValueConverter[Int] {
  override def convert(v: String): Int = v.toInt
}

class StringConverter extends ValueConverter[String] {
  def convert(v: String): String = v
}

class StringListConverter extends ValueConverter[List[String]] {
  override def convert(v: String): List[String] = v.split(",").toList
}

class StringArrayConverter extends ValueConverter[Array[String]] {
  override def convert(v: String): Array[String] = v.split(",")
}
