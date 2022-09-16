package streams.http

import streams.AsyncFunSpec
import cats.effect.IO
import org.scalatest.Inside.inside

import scala.concurrent.duration.DurationInt

class HttpServerTest extends AsyncFunSpec {
  it("should produce a runnable stream") {
    HttpServer().stream
      .interruptAfter(1.seconds)
      .compile
      .drain
      .map(_ => succeed)
  }

  it("should be mergeable with other streams") {
    val otherStream: fs2.Stream[IO, Int] =
      fs2.Stream
        .repeatEval(IO(1))
        .metered(20.milliseconds)
        .take(5)

    // Note: If the server gets a request, fs2.Stream's interruptAfter has no effect!
    HttpServer().stream
      .interruptAfter(3.seconds)
      .zip(otherStream)
      .compile
      .last
      .map { result =>
        inside(result) {
          // interruptAfter causes this to be None
          case None => succeed
        }
      }
  }
}
