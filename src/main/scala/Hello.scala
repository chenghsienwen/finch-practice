import io.finch._, cats.effect.IO
import com.twitter.finagle.Http

object Hello extends App with Endpoint.Module[IO] {
  val api: Endpoint[IO, String] = get("hello") { Ok("Hello, World!") }
  Http.server.serve(":8080", api.toServiceAs[Text.Plain])
}