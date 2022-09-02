package streams.hardening

import cats.MonadError
import cats.implicits._
import cats.effect.{Async, Temporal}
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.language.postfixOps

object RetryConfig {
  implicit val default: RetryConfig = RetryConfig(3, 1 seconds)
}
case class RetryConfig(numberOfTries: Long, initialDelay: FiniteDuration)
object Retries {
  def retried[A, B, F[_]: Async](
      f: A => F[B]
  )(implicit cfg: RetryConfig): A => F[B] = { a: A =>
    retryWithBackoff(f(a), cfg.numberOfTries - 1)(
      Async[F],
      cfg
    )
  }

  def retryWithBackoff[A, F[_]: Async](
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
            b <- retryWithBackoff(f, triesRemaining - 1)(
              Async[F],
              cfg
            )
          } yield b
        } else
          MonadError[F, Throwable].raiseError(error)
      }
}
