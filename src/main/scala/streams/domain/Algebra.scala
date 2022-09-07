package streams.domain
import streams.domain.Models.Core.{Customer, CustomerId, Order, OrderId}
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
  def getCustomer(id: CustomerId): F[Option[Customer]]
  def getOrder(id: OrderId): F[Option[Order]]
}
