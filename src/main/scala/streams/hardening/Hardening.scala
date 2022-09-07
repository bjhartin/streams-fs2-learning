package streams.hardening

import cats.implicits._
import cats.effect.Async
import streams.Refinements._
import streams.hardening.Retries.retried

/*
  Others to include:

    circuitBreaker
    loadShedding / backPressure
 */

class Hardening[F[_]: Async](metrics: Metrics[F], cacheing: Cacheing[F]) {
  import metrics._
  import cacheing._

  // TODO: CacheClient doesn't need to know about A/B.
  def hardened[A, B](
      label: Name
  )(
      f: A => F[Option[B]]
  )(implicit cfg: RetryConfig, cc: Cache[A, B, F]): A => F[Option[B]] = {
    a: A =>
      for {
        cachedLabel <-
          refineF[UnsafeString, Predicates.Name, F](s"${label}_cached")
        uncached = retried(timed(label)(f))(Async[F], cfg)
        hardened = timed(cachedLabel)(cached(uncached))
        result <- hardened(a)
      } yield result
  }
}
