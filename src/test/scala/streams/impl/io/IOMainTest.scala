package streams.impl.io

import org.scalatest.Inside.inside
import streams.AsyncFunSpec
import streams.Refinements.RefinementException

class IOMainTest extends AsyncFunSpec {
  it("should sanitize the args") {
    for {
      result <-
        IOMain
          .run(List("NotAValidArgDueToSpecialChars!@#$%^&"))
          .attempt
    } yield {
      inside(result) {
        case Left(RefinementException(msg)) => assert(msg.contains("matches"))
      }
    }
  }
}
