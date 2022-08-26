package streams

import cats.effect.IO
import fs2.Stream

object Sinks {
  def customerResponses: Stream[IO, Unit] = ???
  def orderResponses: Stream[IO, Unit] = ???
}
