package streams.domain

import java.time.Instant
import java.util.UUID

import streams.domain.Models.Core.{CustomerId, OrderId}
import io.circe
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.refined._
import org.scalacheck.ScalacheckShapeless._
import org.scalacheck.Arbitrary
import org.scalacheck.derive.MkArbitrary
import streams.Refinements._
import streams.Refinements.Scalacheck._

// Quick and dirty models and using Shapeless for auto-derivation which can be slow.
object Models {

  object Core {
    case class CustomerId(value: UUID)
    object CustomerId {
      implicit val decCustomerId: Decoder[CustomerId] =
        deriveDecoder[CustomerId]
      implicit val encCustomerId: Encoder[CustomerId] =
        deriveEncoder[CustomerId]
    }
    case class Customer(id: CustomerId, email: Email, name: Name)

    object Customer {
      implicit lazy val arb: Arbitrary[Customer] =
        MkArbitrary[Customer].arbitrary
      implicit lazy val dec: circe.Decoder[Customer] = deriveDecoder[Customer]
      implicit lazy val enc: circe.Encoder[Customer] = deriveEncoder[Customer]
    }

    case class ItemId(value: UUID)
    object ItemId {
      implicit val encItemId: Encoder[ItemId] = deriveEncoder[ItemId]
      implicit val decItemId: Decoder[ItemId] = deriveDecoder[ItemId]
    }

    case class Item(id: ItemId, name: Name, sku: SKU, price: Price)

    object Item {
      implicit val arb: Arbitrary[Item] = MkArbitrary[Item].arbitrary
      implicit val dec: circe.Decoder[Item] = deriveDecoder[Item]
      implicit val enc: circe.Encoder[Item] = deriveEncoder[Item]
    }

    case class OrderId(value: UUID)
    object OrderId {
      implicit val encOrderId: Encoder[OrderId] = deriveEncoder[OrderId]
      implicit val decOrderId: Decoder[OrderId] = deriveDecoder[OrderId]
    }

    case class DatePlaced(value: Instant)
    object DatePlaced {
      implicit val encDatePlaced: Encoder[DatePlaced] =
        deriveEncoder[DatePlaced]
      implicit val decDatePlaced: Decoder[DatePlaced] =
        deriveDecoder[DatePlaced]
    }

    case class Order(
        id: OrderId,
        custId: CustomerId,
        datePlaced: Instant,
        items: List[Item]
    )

    object Order {
      implicit val arb: Arbitrary[Order] = MkArbitrary[Order].arbitrary
      implicit val dec: circe.Decoder[Order] = deriveDecoder[Order]
      implicit val enc: circe.Encoder[Order] = deriveEncoder[Order]
    }

  }

  object Messages {
    case class CustomerRequest(customerId: CustomerId)
    object CustomerRequest {
      implicit val arb: Arbitrary[CustomerRequest] = {
        MkArbitrary[CustomerRequest].arbitrary
      }
      implicit val dec: circe.Decoder[CustomerRequest] =
        deriveDecoder[CustomerRequest]
      implicit val enc: circe.Encoder[CustomerRequest] =
        deriveEncoder[CustomerRequest]
    }

    sealed trait GetResponse[A]
    case class Found[A](found: A) extends GetResponse[A]
    case object NotFound extends GetResponse[Nothing]

    case class OrderRequest(orderId: OrderId)
    object OrderRequest {
      implicit val arb: Arbitrary[OrderRequest] =
        MkArbitrary[OrderRequest].arbitrary
      implicit val dec: circe.Decoder[OrderRequest] =
        deriveDecoder[OrderRequest]
      implicit val enc: circe.Encoder[OrderRequest] =
        deriveEncoder[OrderRequest]
    }
  }
}
