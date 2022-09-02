package streams

import domain.Models.Core.{Customer, Order}
import domain.Models.Messages.{CustomerRequest, OrderRequest}
import fs2.Pipe
import streams.domain.Algebra

trait Processing[F[_]] {
  def processCustomerRequest: Pipe[F, CustomerRequest, Option[Customer]]
  def processOrderRequest: Pipe[F, OrderRequest, Option[Order]]
}

object Processing {
  def apply[F[_]](alg: Algebra[F]): Processing[F] =
    new Processing[F] {
      // Pipes are transformations on streams, e.g. Stream[F, A] => Stream[F, B].
      // Here is where we compose up all the functions of IO[...] that comprise the 'pipeline'
      // for a given event/request/message/signal/etc.
      //
      // We use the shorthand for a single-method trait/abstract class here, since we really only
      // need to provide a function on a stream of requests.
      def processCustomerRequest: Pipe[F, CustomerRequest, Option[Customer]] =
        _.evalMap { req => alg.getCustomer(req.customerId) }

      def processOrderRequest: Pipe[F, OrderRequest, Option[Order]] =
        _.evalMap { req => alg.getOrder(req.orderId) }
    }
}
