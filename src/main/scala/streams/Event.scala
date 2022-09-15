package streams

import cats.MonadThrow
import streams.Refinements.UnsafeString

case class Event(eventType: UnsafeString, content: UnsafeString)
object Event {
  sealed trait EventType

  case object EventType {
    case class UnknownEventType(value: UnsafeString)
        extends RuntimeException(s"Unknown event type $value")
    case object CustomerRequest extends EventType
    case object OrderRequest extends EventType
    def from[F[_]: MonadThrow](s: UnsafeString): F[EventType] =
      s match {
        case s if s == CustomerRequest.toString =>
          MonadThrow[F].pure(CustomerRequest)
        case s if s == OrderRequest.toString => MonadThrow[F].pure(OrderRequest)
        case other                           => MonadThrow[F].raiseError(UnknownEventType(other))
      }
  }
}
