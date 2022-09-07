package streams

import cats.Applicative
import fs2.Pipe
import streams.Refinements.UnsafeString

/*
  A sink is the final step in a stream.  I'm choosing to define it as the publishing
  of the encoded value to the destination, e.g. writing to SQS queue.

  Possibly I should use a better type than UnsafeString here - I think this will evolve.

  The implementation is simply faked for now to return unit.
 */
trait Sinks[F[_]] {
  def customerResponses: Pipe[F, UnsafeString, Unit]
  def orderResponses: Pipe[F, UnsafeString, Unit]
}

object Sinks {
  def apply[F[_]: Applicative]: Sinks[F] =
    new Sinks[F] {
      def customerResponses: Pipe[F, UnsafeString, Unit] =
        _.evalMap { _ => Applicative[F].pure(()) }

      def orderResponses: Pipe[F, UnsafeString, Unit] =
        _.evalMap { _ => Applicative[F].pure(()) }
    }
}
