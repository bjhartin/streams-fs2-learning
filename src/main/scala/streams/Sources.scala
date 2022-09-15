package streams

import java.util.concurrent.TimeUnit

import cats.effect.Temporal
import cats.implicits._
import fs2.Stream
import io.circe.Encoder
import org.scalacheck.Arbitrary
import streams.Event.EventType
import streams.domain.Models.Messages.{CustomerRequest, OrderRequest}

import scala.concurrent.duration.FiniteDuration

/*
  Here is where I'm defining the source of events that the program must process.
  Currently they are faked, but I think eventually this would be where we organize streams which originate
  via SQS, etc.  For HTTP (via HTTP4S), I'm not quite sure if it would make sense to put those here since
  HTTP4s has its own way.  Hopefully they could be listed here.  Assuming we had to write at least some
  plumbing, e.g. a generic SQS stream, that might not be in this file, but we would still organize
  the sources here for clarity.  At least that's my current thought, because I think there's value in
  being able to see the 'outside world' function calls at a glance.
 */
trait Sources[F[_]] {
  def events: Stream[F, Event]
}

object Sources {
  def apply[F[_]: Temporal]: Sources[F] =
    new Sources[F] {
      override def events: Stream[F, Event] =
        phonyStream[CustomerRequest, F](EventType.CustomerRequest)
          .interleave(phonyStream[OrderRequest, F](EventType.OrderRequest))
    }

  /*
    Randomly spit out a serialized msg from the outside world.
    FS2 has helper methods to do this kind of thing, but I wanted to work from scratch.
    You can see how this could poll a queue, etc.
   */
  private def phonyStream[A, F[_]: Temporal](
      eventType: EventType
  )(implicit
      enc: Encoder[A],
      arb: Arbitrary[A]
  ): Stream[F, Event] = {
    def next: Stream[F, Event] = {
      val req: F[Event] = for {
        _ <- Temporal[F].sleep(
          FiniteDuration((Math.random() * 2).toLong, TimeUnit.MILLISECONDS)
        )
        a <- Temporal[F].fromOption(
          arb.arbitrary.sample,
          new RuntimeException("Could not generate")
        )
      } yield {
        Event(eventType.toString, enc(a).noSpaces)
      }

      Stream.eval(req) ++ next
    }
    next
  }
}
