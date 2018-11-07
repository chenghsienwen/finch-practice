import java.nio.charset.StandardCharsets
import java.util.UUID

import com.twitter.finagle.http.Status
import com.twitter.io.Buf
import domain.http._
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import io.finch.todo.{Todo, TodoNotFound}
import io.finch.todo.TodoMain._
import PromoteMain._
import domain.db.Round
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.prop.Checkers
import org.scalatest.{FlatSpec, Matchers}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import util.EnvUtils._
import util.{GameStatus, IdUtils, TimestampUtils}
import cats.syntax.option._
import domain.SessionId
import util.PipeOperator.Pipe
import shapeless.syntax.typeable._
import docker.DockerRedisService
import FutureUtils._
/**
  * sbt "testOnly PromoteSpec"
  * sbt 'testOnly PromoteSpec -- -z "be ready with container"'
  */
class PromoteSpec extends FlatSpec with Matchers with Checkers with MockitoSugar with TestKit with DockerRedisService {
  startAllOrFail()
  Thread.sleep(2500)
  private[this] val ADMIN_ACCOUNT_ID     = env[Array[String]]("ADMIN_ACCOUNT_ID_WHITELIST").get.head
  private[this] val sessionTTLSecs       = env[Long](REDIS_TTL_SECONDS_ENV).getOrElse(30 * 86400L)
  private[this] val VENDING_ID_LIST      = (0 until 3).map(_ => generateAccountId()).toList
  private[this] val SESSION_ID_LIST      = (0 until 3).map(_ => generateSessionId()).toList
  private[this] val PHONE_ID             = generateAccountId()
  private[this] val PHONE_ID_OTHER       = generateAccountId()
  private[this] val NOT_ADMIN_ACCOUNT_ID = generateAccountId()
  private[this] val GAME_SESSION_ID      = generateSessionId()
  private[this] val APP_TIMESTAMP        = System.currentTimeMillis()
  private[this] val SESSION_HANDSET_ITEM = "testItem"
  private[this] val SESSION_IAMGE_URL    = "http://test.download.image"

  behavior of "mongodb node"
  it should "be ready with container" in {
    isContainerReady(redisContainer).toFutureValue shouldBe true
    removeVending()
  }

  behavior of "the promote endpoint"

  val mockIdUtils             = mock[IdUtils]
  val mockTimeUtils           = mock[TimestampUtils]
  private val mockPromoteRepo = new PromoteRepo(mockIdUtils, mockTimeUtils)

  it should "create a vending id list" in {
    val req = CreateVendingRequest(VENDING_ID_LIST)
    val input = Input
      .post("/api/mwc_promote/v1/admin/game/register_vending")
      .withHeaders(("X-HTC-Account-Id", ADMIN_ACCOUNT_ID))
      .withBody[Application.Json](req, Some(StandardCharsets.UTF_8))
    val res            = createVending(mockPromoteRepo)(input)
    val Some(response) = res.awaitOutputUnsafe()

    response.status === Status.Ok &&
    response.value.vendingIds === req.vendingIds

    removeVending()

  }

  behavior of "create session endpoint"

  it should "create session succeed" in {
    when(mockIdUtils.generateSessionId()).thenReturn(SESSION_ID_LIST.head)
    createVendingAction(VENDING_ID_LIST)
    val request = CreateSessionRequest(VENDING_ID_LIST.head)
    val input =
      Input.post("/api/mwc_promote/v1/game/create").withBody[Application.Json](request, Some(StandardCharsets.UTF_8))
    createSession(mockPromoteRepo)(input).awaitOutputUnsafe().get.value.sessionId shouldBe SESSION_ID_LIST.head
    removeVending()
    removeSession(SESSION_ID_LIST.head)
  }

  behavior of "the patchSession endpoint"

  it should "update session succeed" in {
    when(mockTimeUtils.currentTimestamp()).thenReturn(APP_TIMESTAMP)
    createVendingAction(VENDING_ID_LIST)
    createSessionAction(sessionId = SESSION_ID_LIST.head)

    val request =
      UpdateSessionRequest(status = None, handsetItem = SESSION_HANDSET_ITEM.some, imageUrl = SESSION_IAMGE_URL.some)
    val response = Round(
      _id = SESSION_ID_LIST.head,
      clientId = PHONE_ID.some,
      status = None,
      handsetItem = SESSION_HANDSET_ITEM.some,
      imageUrl = SESSION_IAMGE_URL.some,
      createTime = APP_TIMESTAMP,
      updateTime = APP_TIMESTAMP
    ) |> UpdateSessionResponse
    val input =
      Input
        .patch(s"/api/mwc_promote/v1/game/${SESSION_ID_LIST.head}?clientId=${PHONE_ID}")
        .withBody[Application.Json](request, Some(StandardCharsets.UTF_8))
    updateSession(mockPromoteRepo)(input).awaitOutputUnsafe().get.value shouldBe response
    removeVending()
    removeSession(SESSION_ID_LIST.head)
  }

  behavior of "get session endpoint"

  it should "get session succeed" in {
    when(mockTimeUtils.currentTimestamp()).thenReturn(APP_TIMESTAMP)
    createVendingAction(VENDING_ID_LIST)
    createSessionAction(sessionId = SESSION_ID_LIST.head)

    val response = Round(_id = SESSION_ID_LIST.head, clientId = None, createTime = APP_TIMESTAMP, updateTime = APP_TIMESTAMP) |> GetSessionResponse
    val input =
      Input.get(s"/api/mwc_promote/v1/game/${SESSION_ID_LIST.head}?clientId=${VENDING_ID_LIST.head}")
    getSession(mockPromoteRepo)(input).awaitOutputUnsafe().get.value shouldBe response
    removeVending()
    removeSession(SESSION_ID_LIST.head)
  }

  private def createVendingAction(req: List[String]): Unit = {
    //insert vneidng id
    val result = mockPromoteRepo.insertVending(req |> CreateVendingRequest).unsafeRunSync()
    result.isInstanceOf[CreateVendingSucceed] shouldBe true
    result.cast[CreateVendingSucceed].get.vendingIds shouldBe req
  }

  private def createSessionAction(sessionId: String): Unit = {
    when(mockIdUtils.generateSessionId()).thenReturn(sessionId)
    val result = mockPromoteRepo.insertSession(CreateSessionCacheRequest(sessionTTLSecs, None)).unsafeRunSync()
    result._id shouldBe sessionId
    result.clientId shouldBe None
  }
  def removeVending(): Unit =
    mockPromoteRepo.deleteVending().unsafeRunSync()

  def removeSession(id: String): Unit =
    mockPromoteRepo.deleteSession(id |> SessionId)

}
