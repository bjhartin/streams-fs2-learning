package streams.impl

import cats.effect.IO
import org.scalacheck.Arbitrary._
import streams.domain.Algebra
import streams.domain.Models.Core
import streams.domain.Models.Core.{Customer, Order}

object ArbitraryIOAlgebra {
  def apply(): Algebra[IO] =
    new Algebra[IO] {
      override def getCustomer(id: Core.CustomerId): IO[Option[Core.Customer]] =
        IO {
          arbitrary[Customer].sample
            .map(_.copy(id = id))
        }

      override def getOrder(id: Core.OrderId): IO[Option[Core.Order]] =
        IO {
          arbitrary[Order].sample
            .map(_.copy(id = id))
        }
    }
}
