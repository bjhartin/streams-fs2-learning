package streams.examples

import cats.effect.IO
import cats.effect.unsafe.implicits.global

object ExampleFs2Streams extends App {
  // A recursive function is used to produce integers endlessly.
  // This is called 'co-recursion' or 'unfolding' as it's the opposite in a way.
  // It starts from a base case and produces values, instead of reducing values to a base case.
  val integers: fs2.Stream[IO, Int] = {
    def increment(s: Int): fs2.Stream[IO, Int] =
      fs2.Stream.emit(s) ++ increment(s + 1)

    fs2.Stream.suspend(increment(0))
  }

  // Can do it with the unfold method
  val integers2: fs2.Stream[IO, Int] =
    fs2.Stream.unfold(0)(i => Option((i, i + 1)))

  integers
    .foreach { i =>
      IO.delay {
        println(i)
        Thread.sleep(500)
      }
    }
    .compile
    .drain
    .unsafeRunSync()
}
