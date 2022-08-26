package streams

import cats.effect.IO
import fs2.Pipe
import io.circe.jawn.decode
import io.circe.syntax._
import streams.domain.Models.Core.{Customer, Order}
import streams.domain.Models.Messages.{CustomerRequest, OrderRequest}

/* Events which come into the program are *always* in raw bytes.
   Sometimes our libraries may hide that, converting them to Strings, Json, etc.,
   but they are never well-typed scala classes that we want to use.

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
object Codecs {
  // String is the type from/to which we decode/encode customer requests.
  // Might not be the same for all requests/responses.
  lazy val decodeCustomerReq: Pipe[IO, String, CustomerRequest] =
    _.evalMap(v => IO.fromEither(decode[CustomerRequest](v)))

  lazy val encodeCustomer: Pipe[IO, Option[Customer], String] =
    _.evalMap(v => IO(v.asJson.noSpaces))

  lazy val decodeOrderReq: Pipe[IO, String, OrderRequest] =
    _.evalMap(v => IO.fromEither(decode[OrderRequest](v)))

  lazy val encodeOrder: Pipe[IO, Option[Order], String] =
    _.evalMap(v => IO(v.asJson.noSpaces))
}
