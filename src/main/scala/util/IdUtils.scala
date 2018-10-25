package util

import java.util.UUID
import EnvUtils.redisKeyPrefix
class IdUtils {
  def generateSessionId(): String =
    s"${redisKeyPrefix}-session-${UUID.randomUUID().toString}"

  def generateOptinId(): String = UUID.randomUUID().toString
}
