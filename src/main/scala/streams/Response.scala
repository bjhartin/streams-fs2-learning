package streams

import streams.Refinements.UnsafeString
import streams.Response.ResponseType

/*
  Represents a response going to the outside world.

  In other words, we have invoked their chosen A => F[B], obtained a B and encoded it for their consumption
  as bytes.
 */
case class Response(responseType: ResponseType, content: UnsafeString)
object Response {
  sealed trait ResponseType
  case object CustomerResponse extends ResponseType
  case object OrderResponse extends ResponseType
}
