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
import util.{GameStatus, IdUtils}
import cats.syntax.option._
import util.PipeOperator.Pipe
class PromoteSpec extends FlatSpec with Matchers with Checkers with MockitoSugar with TestKit {
  private[this] val ADMIN_ACCOUNT_ID = env[Array[String]]("ADMIN_ACCOUNT_ID_WHITELIST").get.head
  private[this] val VENDING_ID_LIST  = (0 until 3).map(_ => generateAccountId()).toList
  private[this] val SESSION_ID_LIST      = (0 until 3).map(_ => generateSessionId()).toList
  private[this] val PHONE_ID             = generateAccountId()
  private[this] val PHONE_ID_OTHER       = generateAccountId()
  private[this] val NOT_ADMIN_ACCOUNT_ID = generateAccountId()
  private[this] val GAME_SESSION_ID      = generateSessionId()
  private[this] val APP_TIMESTAMP        = System.currentTimeMillis()
  private[this] val SESSION_HANDSET_ITEM = "testItem"
  private[this] val SESSION_IAMGE_URL    = "http://test.download.image"
  behavior of "the promote endpoint"

  case class TodoWithoutId(title: String, completed: Boolean, order: Int)
  val mockIdUtils = mock[IdUtils]

  def genTodoWithoutId: Gen[TodoWithoutId] =
    for {
      t <- Gen.alphaStr
      c <- Gen.oneOf(true, false)
      o <- Gen.choose(Int.MinValue, Int.MaxValue)
    } yield TodoWithoutId(t, c, o)

  implicit def arbitraryTodoWithoutId: Arbitrary[TodoWithoutId] = Arbitrary(genTodoWithoutId)

  it should "create a todo" in {
    check { todoWithoutId: TodoWithoutId =>
      val input = Input
        .post("/todos")
        .withBody[Application.Json](todoWithoutId, Some(StandardCharsets.UTF_8))

      val res        = postTodo(input)
      val Some(todo) = res.awaitOutputUnsafe()

      todo.status === Status.Created &&
      todo.value.completed === todoWithoutId.completed &&
      todo.value.title === todoWithoutId.title &&
      todo.value.order === todoWithoutId.order &&
      Todo.get(todo.value.id).isDefined
    }
  }
  def genVendingId: Gen[CreateVendingRequest] =
    for {
      id <- Gen.uuid
    } yield CreateVendingRequest(List(id.toString))
  implicit def arbitraryVendingId: Arbitrary[CreateVendingRequest] = Arbitrary(genVendingId)
  def genSession: Gen[Round] =
    for {
      id <- Gen.uuid
    } yield Round(_id = id.toString)
  implicit def arbitrarySession: Arbitrary[Round] = Arbitrary(genSession)

  it should "create a vending id list" in {
    check { req: CreateVendingRequest =>
      val input = Input
        .post("/api/mwc_promote/v1/admin/game/register_vending")
        .withHeaders(("X-HTC-Account-Id", ADMIN_ACCOUNT_ID))
        .withBody[Application.Json](req, Some(StandardCharsets.UTF_8))
      val res            = createVending(input)
      val Some(response) = res.awaitOutputUnsafe()

      response.status === Status.Ok &&
      response.value.vendingIds === req.vendingIds
    }
  }

  behavior of "create session endpoint"

  it should "create session succeed" in {
    when(mockIdUtils.generateSessionId()).thenReturn(SESSION_ID_LIST.head)
    val request = CreateSessionRequest(createVendingAction().head)
    val input =
      Input.post("/api/mwc_promote/v1/game/create").withBody[Application.Json](request, Some(StandardCharsets.UTF_8))
    createVending(input).awaitOutputUnsafe() shouldBe Some(SESSION_ID_LIST.head)
    removeVending()
  }

  behavior of "the patchSession endpoint"

  it should "update session succeed" in {
    createSessionAction()

    val request = UpdateSessionRequest(status = None, handsetItem = SESSION_HANDSET_ITEM.some, imageUrl = SESSION_IAMGE_URL.some)
    val response = Round(
      _id = SESSION_ID_LIST.head,
      clientId = PHONE_ID.some,
      status = GameStatus.HANDSET_READY.some,
      handsetItem = None,
      imageUrl = None,
      createTime = APP_TIMESTAMP,
      updateTime = APP_TIMESTAMP
    ) |> UpdateSessionResponse
    val input =
      Input.patch(s"/api/mwc_promote/v1/${SESSION_ID_LIST.head}?clientId=${VENDING_ID_LIST.head}").withBody[Application.Json](request, Some(StandardCharsets.UTF_8))
    updateSession(input).awaitOutputUnsafe() shouldBe Some(response)
    removeVending()
    removeSession()
  }

  behavior of "get session endpoint"

  it should "get session succeed" in {
    createVendingAction()
    createSessionAction()

    val request = UpdateSessionRequest(status = None, handsetItem = SESSION_HANDSET_ITEM.some, imageUrl = SESSION_IAMGE_URL.some)
    val response = Round(_id = SESSION_ID_LIST.head, createTime = APP_TIMESTAMP, updateTime = APP_TIMESTAMP) |> GetSessionResponse
    val input =
      Input.get(s"/api/mwc_promote/v1/${SESSION_ID_LIST.head}?clientId=${VENDING_ID_LIST.head}")
    getSession(input).awaitOutputUnsafe() shouldBe Some(response)
    removeVending()
    removeSession()
  }

  behavior of "delete session endpoint"

  it should "delete session succeed by admin" in {
    createVendingAction()
    createSessionAction()

    val response   = DeleteSessionResponse(List(SESSION_ID_LIST.head))
    val input =
      Input.delete(s"/api/mwc_promote/v1/admin/game/delete?sessionId=${SESSION_ID_LIST.head}").withHeaders(("X-HTC-Account-Id", ADMIN_ACCOUNT_ID))
    deleteSession(input).awaitOutputUnsafe() shouldBe Some(response)
    removeVending()
  }

  private def createTodo(): Todo = {
    val todo = Todo(UUID.randomUUID(), "foo", completed = false, 0)
    Todo.save(todo)
    todo
  }

  private def createVendingAction(): List[String] =
    //insert vneidng id
    VENDING_ID_LIST
  private def createSessionAction(): String = {
    when(mockIdUtils.generateSessionId()).thenReturn(SESSION_ID_LIST.head)
    SESSION_ID_LIST.head
  }
  def removeVending(): Unit = {

  }

  def removeSession(): Unit = {

  }

}
