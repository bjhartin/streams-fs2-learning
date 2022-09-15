package streams

import cats.Applicative
import fs2.Pipe

/*
  A sink is the final step in a stream.  I'm choosing to define it as the publishing
  of the encoded value to the destination, e.g. writing to SQS queue.

  Possibly I should use a better type than Response here - I think this will evolve.

  The implementation is simply faked for now to return unit.
 */
trait Sinks[F[_]] {
  def responses: Pipe[F, Response, Unit]
}

object Sinks {
  def apply[F[_]: Applicative]: Sinks[F] =
    new Sinks[F] {
      def responses: Pipe[F, Response, Unit] =
        _.evalMap { _ => Applicative[F].pure(()) }
    }
}
