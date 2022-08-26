package streams

import cats.effect.IO
import domain.Models.Core.{Customer, Order}
import domain.Models.Messages.{CustomerRequest, OrderRequest}
import fs2.Pipe
import org.scalacheck.Arbitrary._

object Processing {
  // Pipes are transformations on streams, e.g. Stream[F, A] => Stream[F, B].
  // Here is where we compose up all the functions of IO[...] that comprise the 'pipeline'
  // for a given event/request/message/signal/etc.
  def processCustomerRequest: Pipe[IO, CustomerRequest, Option[Customer]] = {
    _.evalMap { req =>
      IO {
        arbitrary[Customer].sample
          .map(_.copy(id = req.customerId))
      }
    }
  }

  def processOrderRequest: Pipe[IO, OrderRequest, Option[Order]] = {
    _.evalMap { req =>
      IO {
        arbitrary[Order].sample
          .map(_.copy(id = req.orderId))
      }
    }
  }
}
