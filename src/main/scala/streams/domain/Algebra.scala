package streams.domain
import streams.domain.Models.Core.{Customer, CustomerId, Order, OrderId}
/*
  Defines the signatures for functions that process events from the outside world.

  These are the 'entry points' of the program (apart from the main method, of course).

  Each of these will have been preceded by decoding the event from bytes and rejecting it if invalid.

  Of course this could be decomposed into smaller pieces, but it should be kept apart from the implementation.

  This, together with the models, is the streams.domain/algebra of the application.

  I think the streams.domain/algebra of an app often corresponds to its entry points.

  In fact, we could generate contracts from this (and some do).
 */
trait Algebra[F[_]] {
  def getCustomer(id: CustomerId): F[Option[Customer]]
  def getOrder(id: OrderId): F[Option[Order]]
}
