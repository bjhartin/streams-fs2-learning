package streams.hardening

import cats.implicits._
import cats.effect.Async
import streams.Cache
import streams.Refinements._
import streams.hardening.Retries.RetryConfig

trait Hardening[F[_]] {
  def hardened[A, B](
      label: Name
  )(
      f: A => F[Option[B]]
  )(implicit
      cfg: RetryConfig,
      cc: Cache[A, B, F],
      fp: FailurePercentage
  ): A => F[Option[B]]
}
/*
  Others to include:

    circuitBreaker
    loadShedding / backPressure
 */

object Hardening {
  def apply[F[_]: Async](
      metrics: Metrics[F],
      cacheing: Cacheing[F],
      retries: Retries[F],
      chaos: Chaos[F]
  ): Hardening[F] =
    new Hardening[F] {

      import metrics._
      import cacheing._
      import chaos._
      import retries._

      def hardened[A, B](
          label: Name
      )(
          f: A => F[Option[B]]
      )(implicit
          cfg: RetryConfig,
          cc: Cache[A, B, F],
          failurePercentage: FailurePercentage
      ): A => F[Option[B]] = { a: A =>
        for {
          cachedLabel <- refineStringF[Predicates.Name, F](
            s"${label}_cached"
          ) // Have to refine to append
          uncached = retried(timed(label)(f))(cfg)
          hardened = (timed(cachedLabel)(cached(chaotic(uncached))))
          result <- hardened(a)
        } yield result
      }
    }
}
