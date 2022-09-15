package streams.hardening

import cats.effect.IO
import streams.AnyFunSpec
import streams.Refinements._

class ChaosTest extends AnyFunSpec {
  private val f = { s: String => IO(s.length) }
  private def chaotic(p: FailurePercentage): String => IO[Int] =
    Chaos[IO].chaotic(f)(p)

  it("should cause functions to fail a certain percentage of the time") {
    for {
      p <- refineF[Float, Predicates.Percentage, IO](100.0f)
      r <- chaotic(FailurePercentage(p))("test").attempt
    } yield {
      assert(r.isLeft)
    }
  }

  it("should not cause the function to fail if we use zero") {
    for {
      p <- refineF[Float, Predicates.Percentage, IO](0.0f)
      r <- chaotic(FailurePercentage(p))("test").attempt
    } yield {
      assert(r.isRight)
    }
  }
}
