package streams.hardening

import cats.implicits._
import cats.effect.Sync
import streams.Refinements.Percentage

import scala.util.Random

case class FailurePercentage(value: Percentage)
trait Chaos[F[_]] {
  def chaotic[A, B](f: A => F[B])(implicit p: FailurePercentage): A => F[B]
}

object Chaos {
  case class ChaosException(p: FailurePercentage)
      extends RuntimeException(
        s"Function failed because it was configured to fail ${p.value} percent of the time"
      )
  def apply[F[_]: Sync]: Chaos[F] =
    new Chaos[F] {
      override def chaotic[A, B](f: A => F[B])(implicit
          p: FailurePercentage
      ): A => F[B] = { a =>
        for {
          chance <- Sync[F].delay {
            Random.nextFloat()
          }
          r <-
            if (chance * 100.0 < p.value.value) {
              Sync[F]
                .delay {
                  println(
                    s"Raising simulated error, just to test our system's fault tolerance"
                  )
                }
                .flatMap { _ =>
                  Sync[F].raiseError[B](ChaosException(p))
                }
            } else
              f(a)
        } yield r
      }
    }
}
