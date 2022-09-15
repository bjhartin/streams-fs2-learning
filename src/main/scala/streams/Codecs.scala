package streams

import cats.implicits._
import cats.MonadThrow
import io.circe.jawn.decode
import io.circe.syntax._
import streams.Response.ResponseType
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
  def decodeCustomerReq(req: Event): F[CustomerRequest]
  def encodeCustomerResp(resp: Either[Throwable, Option[Customer]]): F[Response]
  def decodeOrderReq(req: Event): F[OrderRequest]
  def encodeOrderResp(resp: Either[Throwable, Option[Order]]): F[Response]
}
object Codecs {
  def apply[F[_]: MonadThrow]: Codecs[F] =
    new Codecs[F] {

      import Refinements._

      case class DecodingException(msg: SafeString)
          extends RuntimeException(msg)

      override def decodeCustomerReq(req: Event): F[CustomerRequest] =
        MonadThrow[F]
          .fromEither(decode[CustomerRequest](req.content))
          .handleErrorWith { e =>
            MonadThrow[F].raiseError[CustomerRequest](e)
          }

      override def encodeCustomerResp(
          resp: Either[Throwable, Option[Customer]]
      ): F[Response] =
        resp match {
          case Left(err) =>
            MonadThrow[F].pure(
              Response(ResponseType.Failure, err.toString)
            )
          case Right(resp) =>
            MonadThrow[F].pure(
              Response(ResponseType.Success, resp.asJson.noSpaces)
            )
        }

      override def decodeOrderReq(req: Event): F[OrderRequest] =
        MonadThrow[F]
          .fromEither(decode[OrderRequest](req.content))
          .handleErrorWith { e =>
            MonadThrow[F].raiseError[OrderRequest](e)
          }

      override def encodeOrderResp(
          resp: Either[Throwable, Option[Order]]
      ): F[Response] =
        resp match {
          case Left(err) =>
            MonadThrow[F].pure(
              Response(ResponseType.Failure, err.toString)
            )
          case Right(resp) =>
            MonadThrow[F].pure(
              Response(ResponseType.Success, resp.asJson.noSpaces)
            )
        }
    }
}
