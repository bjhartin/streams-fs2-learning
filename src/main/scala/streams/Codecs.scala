package streams

import cats.MonadThrow
import fs2.Pipe
import io.circe.jawn.decode
import io.circe.syntax._
import streams.Refinements.UnsafeString
import streams.domain.Models.Core.{Customer, Order}
import streams.domain.Models.Messages.{CustomerRequest, OrderRequest}

/* Events which come into the program are *always* in raw bytes.
   Sometimes our libraries may hide that, converting them to Strings, Json, etc.,
   but they are never well-typed models that we want to use.

   Before we can process an event, we have to decode the bytes, which can always fail.
   We want to reject invalid messages and keep our models pure and clear.

   Likewise, when we respond to the outside world, we need to *encode*.

   Defining codecs as pipes allows these to be composed easily.

   These would be of the same form, even if they involved multiple steps like:

   - base64 decoding
   - unzipping
   - proto decoding
     - nested proto decoding
 */
trait Codecs[F[_]] {
  def decodeCustomerReq: Pipe[F, UnsafeString, CustomerRequest]
  def encodeCustomer: Pipe[F, Option[Customer], UnsafeString]
  def decodeOrderReq: Pipe[F, UnsafeString, OrderRequest]
  def encodeOrder: Pipe[F, Option[Order], UnsafeString]
}
object Codecs {
  def apply[F[_]: MonadThrow]: Codecs[F] =
    new Codecs[F] {

      import Refinements._

      case class DecodingException(msg: ErrorMessage)
          extends RuntimeException(msg)

      // UnsafeString is the type from/to which we decode/encode customer requests.
      // Might not be the same for all requests/responses.
      lazy val decodeCustomerReq: Pipe[F, UnsafeString, CustomerRequest] =
        _.evalMap(v => MonadThrow[F].fromEither(decode[CustomerRequest](v)))

      lazy val encodeCustomer: Pipe[F, Option[Customer], UnsafeString] =
        _.evalMap(v => MonadThrow[F].pure(v.asJson.noSpaces))

      lazy val decodeOrderReq: Pipe[F, UnsafeString, OrderRequest] =
        _.evalMap(v => MonadThrow[F].fromEither(decode[OrderRequest](v)))

      lazy val encodeOrder: Pipe[F, Option[Order], UnsafeString] =
        _.evalMap(v => MonadThrow[F].pure(v.asJson.noSpaces))
    }
}
