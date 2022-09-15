package streams.domain
import cats.effect.Sync
import streams.domain.Models.Core
import streams.domain.Models.Core.{Customer, Order}
import streams.domain.Models.Messages.{CustomerRequest, OrderRequest}
/*
  Defines the signatures for functions that are the core business of the program.
  Almost always, these correspond to the 'entry points' of the program.

  'Entry points', i.e. HTTP endpoints, messages, etc. can be viewed as the outside world wanting
  to invoke some function A => F[B].

  Each of these will have been preceded by decoding the event from bytes and rejecting it if invalid.

  Of course this could be decomposed into smaller pieces, but it should be kept apart from the implementation.

  This, together with the models, is the domain/algebra of the application.

  I think the domain/algebra of an app often corresponds to its entry points.

  In fact, we could generate contracts from this (and some do).
 */
trait Algebra[F[_]] {
  def getCustomer(req: CustomerRequest): F[Option[Customer]]
  def getOrder(req: OrderRequest): F[Option[Order]]
}

object Algebra {
  import org.scalacheck.Arbitrary._

  def apply[F[_]: Sync]: Algebra[F] =
    new Algebra[F] {
      override def getCustomer(
          req: CustomerRequest
      ): F[Option[Core.Customer]] =
        Sync[F].delay {
          arbitrary[Customer].sample
            .map(_.copy(id = req.customerId))
        }

      override def getOrder(req: OrderRequest): F[Option[Core.Order]] =
        Sync[F].delay {
          arbitrary[Order].sample
            .map(_.copy(id = req.orderId))
        }
    }
}
