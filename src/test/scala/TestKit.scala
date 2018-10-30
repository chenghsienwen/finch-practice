import java.util.UUID

import util.{IdUtils, TimestampUtils}

trait TestKit {

  private[this] val testTimestampUtils = new TimestampUtils
  private[this] val testIdUtils        = new IdUtils

  def generateAccountId(): String = UUID.randomUUID().toString

  def generateSessionId(): String = testIdUtils.generateSessionId()

  def currentTimestamp(): Long = testTimestampUtils.currentTimestamp()
}
