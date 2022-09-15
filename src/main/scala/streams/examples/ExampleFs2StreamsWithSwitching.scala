package streams.examples

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import fs2.Pipe

/*
  Shows an example of processing a stream conditionally, as we will need when a stream of events/msgs
  has a 'message type' field which tells us the A to use for decode[A] and process[A].

  To simulate this in a minimal case, we will produce the integers as our source and we will branch as follows:

  - Even ints are doubled
  - Odd ints incremented by 1

  So the stream 1,2,3,4,5 should produce 2,4,4,8,6
 */
object ExampleFs2StreamsWithSwitching extends App {
  val integers: fs2.Stream[IO, Int] =
    fs2.Stream.unfold(0)(i => Option((i, i + 1)))

  val oddPipe: Pipe[IO, Int, Int] = { stream =>
    stream.map { i => i + 1 }
  }

  val evenPipe: Pipe[IO, Int, Int] = { stream =>
    stream.map { i => i * 2 }
  }

  /*
    Problem: This decision is only ever made for the first element, after which the stream behavior
    is either evenPipe or oddPipe, and the next elements never encounter the switching behavior.

    We need the choice to be made for each element.
   */
  val results = integers
    .through { stream =>
      stream.flatMap { i =>
        if (i % 2 == 0)
          stream.through(evenPipe)
        else
          stream.through(oddPipe)
      }
    }
    .take(5)
    .compile
    .toList
    .unsafeRunSync()

  println(results)

  // 0*2, 1+1, 2*2, 3+1, 4*2
  assert(List(0, 2, 4, 4, 8) == results)
}
