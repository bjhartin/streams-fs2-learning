package streams

import cats.Applicative
import fs2.Stream

trait Sinks[F[_]] {
  def customerResponses: Stream[F, Unit]
  def orderResponses: Stream[F, Unit]
}

object Sinks {
  def apply[F[_]: Applicative](): Sinks[F] =
    new Sinks[F] {
      def customerResponses: Stream[F, Unit] =
        Stream.eval(Applicative[F].pure(()))
      def orderResponses: Stream[F, Unit] = Stream.eval(Applicative[F].pure(()))
    }
}
