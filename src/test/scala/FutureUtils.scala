import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.{Seconds, Span}

import scala.concurrent.Future

object FutureUtils {
  implicit val timeout = Timeout(Span(10, Seconds))

  implicit class ScalaFutureOps[T](a: Future[T]) {
    def toFutureValue =
      a.futureValue(timeout)
  }
}
