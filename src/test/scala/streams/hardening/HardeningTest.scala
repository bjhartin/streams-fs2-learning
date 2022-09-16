package streams.hardening

import cats.effect.{IO, Sync}
import streams.Refinements.{Predicates, refineF, refineStringF}
import streams.hardening.Retries.RetryConfig
import streams.{AsyncFunSpec, Cache}

class HardeningTest extends AsyncFunSpec {
  import Metrics._

  private implicit val mb = metricsBuilder
  private implicit val fp = refineF[Float, Predicates.Percentage, IO](0f)
  private def cache(result: Option[Int]): Cache[Int, Int, IO] =
    new Cache[Int, Int, IO] {
      override def get(a: Int): IO[Option[Int]] = IO.pure(result)
      override def put(a: Int, b: Int): IO[Unit] = IO.pure(())
    }

  private val metrics = Metrics[IO]
  private val hardening =
    Hardening[IO](metrics, Cacheing[IO], Retries[IO], Chaos[IO])
  private val function = { i: Int =>
    IO.delay(Some(i))
  }

  it("should apply timing to a function") {
    for {
      p <- fp.map(FailurePercentage)
      name <- refineStringF[Predicates.Name, IO]("function1")
      cachedName <- refineStringF[Predicates.Name, IO]("function1_cached")
      hardenedFunction =
        hardening
          .hardened(name)(function)(RetryConfig.default, cache(Some(1)), p)
      result <- hardenedFunction(1)
      cachedTimer <- metrics.getTimer(cachedName)
    } yield {
      assert(result.contains(1))
      assert(cachedTimer.getCount == 1)
      assert(cachedTimer.getMeanRate > 0.0)
    }
  }

  it("should apply timing to both the cached and uncached function") {
    for {
      p <- fp.map(FailurePercentage)
      name <- refineStringF[Predicates.Name, IO]("function2")
      hardenedFunction =
        hardening
          .hardened(name)(function)(RetryConfig.default, cache(None), p)
      result <- hardenedFunction(1)
      timer <- metrics.getTimer(name)
      cachedTimer <- metrics.getTimer(name)
    } yield {
      assert(result.contains(1))
      assert(cachedTimer.getCount == 1)
      assert(cachedTimer.getMeanRate > 0.0)
      assert(timer.getCount == 1)
      assert(timer.getMeanRate > 0.0)
    }
  }

  it("should apply retries to the uncached function") {
    var count = 0
    val function = { _: Int =>
      Sync[IO]
        .delay(count += 1)
        .flatMap { _ =>
          IO.raiseError[Option[Int]](BOOM)
        }
    }
    for {
      p <- fp.map(FailurePercentage)
      name <- refineStringF[Predicates.Name, IO]("function3")
      hardenedFunction =
        hardening
          .hardened(name)(function)(RetryConfig.default, cache(None), p)
      result <- hardenedFunction(1).attempt
    } yield {
      assert(result.isLeft)
      assert(count == RetryConfig.default.numberOfTries)
    }
  }
}
