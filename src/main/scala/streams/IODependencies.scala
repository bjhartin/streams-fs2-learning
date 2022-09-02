package streams

import cats.effect.IO
import streams.impl.ArbitraryIOAlgebra

object IODependencies {
  def wireDependencies: IO[Pipes[IO]] =
    IO.delay {
      val codecs = Codecs[IO]
      val alg = ArbitraryIOAlgebra()
      val processing = Processing[IO](alg)
      Pipes[IO](processing, codecs)
    }
}
