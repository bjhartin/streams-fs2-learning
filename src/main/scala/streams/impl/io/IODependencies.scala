package streams.impl.io

import cats.effect.IO
import streams.{Codecs, Pipes, Processing, Sinks}

object IODependencies {
  def wireDependencies: IO[Pipes[IO]] =
    IO.delay {
      val codecs = Codecs[IO]
      val alg = IOAlgebra()
      val processing = Processing[IO](alg)
      val sinks = Sinks[IO]
      Pipes[IO](processing, codecs, sinks)
    }
}
