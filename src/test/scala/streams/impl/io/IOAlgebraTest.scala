package streams.impl.io

import java.util.UUID

import streams.AsyncFunSpec
import streams.domain.Models.Core.{CustomerId, OrderId}

class IOAlgebraTest extends AsyncFunSpec {
  val algebra = IOAlgebra()

  it("should get a customer") {
    algebra.getCustomer(CustomerId(UUID.randomUUID())).map { c =>
      assert(c.isDefined)
    }
  }

  it("should get an order") {
    algebra.getOrder(OrderId(UUID.randomUUID())).map { c =>
      assert(c.isDefined)
    }
  }
}
