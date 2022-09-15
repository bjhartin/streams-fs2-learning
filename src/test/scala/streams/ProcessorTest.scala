package streams

import cats.effect.IO
import streams.Response.ResponseType

class ProcessorTest extends AsyncFunSpec {
  it("should process an A") {
    val p = Processor.apply[IO, String, String](
      _ => IO("1"),
      _ => IO("1"),
      _ => IO(Response(ResponseType.Success, "1"))
    )
    p.apply(Event("x", "abc")).map { r =>
      assertResult("1")(r.content)
    }
  }
}
