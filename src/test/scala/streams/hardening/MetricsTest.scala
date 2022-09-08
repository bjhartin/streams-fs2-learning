package streams.hardening

import cats.effect.IO
import eu.timepit.refined.auto._
import streams.AsyncFunSpec

import scala.concurrent.duration._
import scala.language.postfixOps

class MetricsTest extends AsyncFunSpec {
  import Metrics._

  private implicit val mb = metricsBuilder
  private val metrics = Metrics[IO]
  private val timedFunction: Unit => IO[Unit] =
    metrics.timed("name") { _ => IO.sleep(100 millis) }

  it("Times a function") {
    for {
      _ <- timedFunction(())
      timer <- metrics.getTimer("name")
    } yield {
      assert(timer.getCount == 1)
      assert(timer.getMeanRate > 0.0)
    }
  }
}
