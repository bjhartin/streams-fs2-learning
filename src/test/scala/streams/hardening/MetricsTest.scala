package streams.hardening

import cats.effect.IO
import streams.AsyncFunSpec
import streams.Refinements.{Name, Predicates, refineStringF}

import scala.concurrent.duration._
import scala.language.postfixOps

class MetricsTest extends AsyncFunSpec {
  import Metrics._

  private implicit val mb = metricsBuilder
  private val metrics = Metrics[IO]
  private def timedFunction(name: Name): Unit => IO[Unit] =
    metrics.timed(name) { _ => IO.sleep(100 millis) }

  it("Times a function") {
    for {
      name <- refineStringF[Predicates.Name, IO]("name")
      _ <- timedFunction(name)(())
      timer <- metrics.getTimer(name)
    } yield {
      assert(timer.getCount == 1)
      assert(timer.getMeanRate > 0.0)
    }
  }
}
