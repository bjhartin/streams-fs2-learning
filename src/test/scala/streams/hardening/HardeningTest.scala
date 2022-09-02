package streams.hardening

import cats.effect.{IO, Sync}
import eu.timepit.refined.auto._
import streams.AsyncFunSpec

class HardeningTest extends AsyncFunSpec {
  import Metrics._
  import RetryConfig._

  private implicit val mb = metricsBuilder
  private implicit val cacheHitClient: Cache[Int, Int, IO] =
    new Cache[Int, Int, IO] {
      override def get(a: Int): IO[Option[Int]] = IO.pure(Some(1))
      override def put(a: Int, b: Int): IO[Unit] = IO.pure(())
    }

  val cacheMissClient = new Cache[Int, Int, IO] {
    override def get(a: Int): IO[Option[Int]] = IO.pure(None)
    override def put(a: Int, b: Int): IO[Unit] = IO.pure(())
  }
  private val cacheing = new Cacheing[IO]
  private val metrics = new Metrics[IO]
  private val hardening = new Hardening[IO](metrics, cacheing)
  private val function = { i: Int =>
    IO.delay(Some(i))
  }
  private val hardenedFunction = hardening.hardened("function")(function)

  it("should apply timing to a function") {
    for {
      result <- hardenedFunction(1)
      cachedTimer <- metrics.getTimer("function_cached")
    } yield {
      assert(result.contains(1))
      assert(cachedTimer.getCount == 1)
      assert(cachedTimer.getMeanRate > 0.0)
    }
  }

  it("should apply timing to both the cached and uncached function") {
    val hardenedFunction =
      hardening
        .hardened("function2")(function)(RetryConfig.default, cacheMissClient)

    for {
      result <- hardenedFunction(1)
      timer <- metrics.getTimer("function2")
      cachedTimer <- metrics.getTimer("function2_cached")
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
    val hardenedFunction =
      hardening
        .hardened("function3")(function)(RetryConfig.default, cacheMissClient)

    for {
      result <- hardenedFunction(1).attempt
    } yield {
      assert(result.isLeft)
      assert(count == RetryConfig.default.numberOfTries)
    }
  }
}
