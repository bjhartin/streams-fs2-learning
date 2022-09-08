package streams.hardening

import cats.implicits._
import cats.effect.Sync
import streams.Refinements.Percentage

import scala.util.Random

trait Chaos[F[_]] {
  def chaotic[A, B](failPercentage: Percentage)(f: A => F[B]): A => F[B]
}

object Chaos {
  case class ChaosException(p: Percentage)
      extends RuntimeException(
        s"Function failed because it was configured to fail $p percent of the time"
      )
  def apply[F[_]: Sync]: Chaos[F] =
    new Chaos[F] {
      override def chaotic[A, B](
          failPercentage: Percentage
      )(f: A => F[B]): A => F[B] = { a =>
        for {
          chance <- Sync[F].delay {
            Random.nextFloat()
          }
          r <-
            if (chance * 100.0 < failPercentage.value)
              Sync[F].raiseError[B](ChaosException(failPercentage))
            else
              f(a)
        } yield r
      }
    }
}
