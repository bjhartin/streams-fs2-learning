package streams.impl.io

import java.util.UUID

import cats.effect.IO
import streams.AsyncFunSpec
import streams.domain.Algebra
import streams.domain.Models.Core.{CustomerId, OrderId}
import streams.domain.Models.Messages.{CustomerRequest, OrderRequest}

class AlgebraTest extends AsyncFunSpec {
  private val algebra = Algebra[IO]

  it("should get a customer") {
    algebra.getCustomer(CustomerRequest(CustomerId(UUID.randomUUID()))).map {
      c =>
        assert(c.isDefined)
    }
  }

  it("should get an order") {
    algebra.getOrder(OrderRequest(OrderId(UUID.randomUUID()))).map { c =>
      assert(c.isDefined)
    }
  }
}
