package streams

import cats.implicits._
import cats.effect.Sync
import fs2.Pipe
import streams.Event.EventType

/*
  When an event arrives, it is telling us that the 'outside world' wants to invoke
  some function A => F[B].  To do this, we must first determine *which* function.
  This can fail - the event type might be invalid.  The main function of this code
  is to decode this to a valid EventType, then choose and invoke the (well-typed) processor.
 */
trait Pipeline[F[_]] {
  def routeAndProcess: Pipe[F, Event, Response]
}

object Pipeline {
  def apply[F[_]: Sync](
      routing: EventType => Processor[F, _, _]
  ): Pipeline[F] =
    new Pipeline[F] {
      def routeAndProcess: Pipe[F, Event, Response] = { stream =>
        stream.evalMap { e =>
          for {
            eventType <- EventType.from(e.eventType)(Sync[F])
            processor =
              routing(
                eventType
              ) // Here, the routing function determines the A/B types.
            result <-
              processor(e) // decodes to an A, invokes A => F[B], encodes from B
          } yield result
        }
      }
    }
}
