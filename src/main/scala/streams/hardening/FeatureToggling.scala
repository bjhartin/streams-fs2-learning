package streams.hardening

import cats.implicits._
import cats.effect.Sync
import streams.Refinements.Name
import streams.hardening.FeatureToggling.FeatureToggles
import streams.hardening.FeatureToggling.FeatureToggles.{
  FeatureToggleResult,
  ToggledOff,
  ToggledOn
}

/*
  This makes the assumption that when we feature toggle a behavior, we choose between two functions
  with the same signature.  Will this hold up for all situations?  I think so, because we could introduce
  a sum type (sealed trait), if necessary, for the return type B.
 */
trait FeatureToggling[F[_]] {
  def toggled[A, B](toggle: Name)(on: A => F[B])(off: A => F[B])(implicit
      toggles: FeatureToggles[F]
  ): A => F[B]
}

object FeatureToggling {
  // We do not assume, or expose, any particular implementation details.
  // This could be an HTTP client, Redis, etc.
  trait FeatureToggles[F[_]] {
    def getToggle(featureToggleName: Name): F[FeatureToggleResult]
  }

  object FeatureToggles {
    sealed trait FeatureToggleResult
    case object ToggledOn extends FeatureToggleResult
    case object ToggledOff extends FeatureToggleResult
  }

  def apply[F[_]: Sync]: FeatureToggling[F] =
    new FeatureToggling[F] {
      override def toggled[A, B](toggle: Name)(
          on: A => F[B]
      )(off: A => F[B])(implicit toggles: FeatureToggles[F]): A => F[B] = { a =>
        for {
          result <- toggles.getToggle(toggle)
          b <- result match {
            case ToggledOn  => on(a)
            case ToggledOff => off(a)
          }
        } yield b
      }
    }
}
