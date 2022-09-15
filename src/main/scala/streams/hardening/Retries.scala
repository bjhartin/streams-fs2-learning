package streams.hardening

import cats.MonadError
import cats.implicits._
import cats.effect.{Async, Temporal}
import streams.hardening.Retries.RetryConfig

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.language.postfixOps

trait Retries[F[_]] {
  def retried[A, B](f: A => F[B])(implicit cfg: RetryConfig): A => F[B]
}

object Retries {
  object RetryConfig {
    implicit val default: RetryConfig = RetryConfig(3, 1 seconds)
  }
  case class RetryConfig(numberOfTries: Long, initialDelay: FiniteDuration)

  def apply[F[_]: Async]: Retries[F] =
    new Retries[F] {
      def retried[A, B](f: A => F[B])(implicit cfg: RetryConfig): A => F[B] = {
        a: A =>
          retryWithBackoff(f(a), cfg.numberOfTries - 1)
      }

      private def retryWithBackoff[A](
          f: => F[A],
          triesRemaining: Long
      )(implicit cfg: RetryConfig): F[A] =
        MonadError[F, Throwable]
          .handleErrorWith(f) { error =>
            if (triesRemaining > 0) {
              for {
                _ <- Temporal[F].sleep(
                  cfg.initialDelay * (cfg.numberOfTries - triesRemaining)
                )
                b <- retryWithBackoff(f, triesRemaining - 1)
              } yield b
            } else
              MonadError[F, Throwable].raiseError(error)
          }
    }
}
