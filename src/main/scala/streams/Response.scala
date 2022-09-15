package streams

import streams.Refinements.UnsafeString
import streams.Response.ResponseType

/*
  Represents a response going to the outside world, encoded and ready to send.

  In other words, we have invoked their chosen A => F[B], obtained a B and encoded it for their consumption
  as bytes.
 */
case class Response(responseType: ResponseType, content: UnsafeString)
case object Response {
  sealed trait ResponseType
  object ResponseType {
    case object Success extends ResponseType
    case object Failure extends ResponseType
  }
}
