package streams.impl.io

import cats.effect.IO
import nl.grons.metrics4.scala.MetricBuilder
import streams.Event.EventType
import streams.domain.Algebra
import streams.domain.Models.Core.{Customer, Order}
import streams.domain.Models.Messages.{CustomerRequest, OrderRequest}
import streams.hardening.{
  Cacheing,
  Chaos,
  FailurePercentage,
  Hardening,
  Metrics,
  Retries
}
import streams.{Cache, Codecs, Pipeline, Processor, Sinks, Sources}
import streams.hardening.Retries.RetryConfig

object IODependencies {
  case class IODependencies(
      sources: Sources[IO],
      routing: Pipeline[IO],
      sinks: Sinks[IO]
  )

  def wireDependencies: IO[IODependencies] = {
    import eu.timepit.refined.auto._
    IO.delay {
      import RetryConfig.default
      implicit val metricsBuilder: MetricBuilder = Metrics.metricsBuilder
      implicit val failurePercentage: FailurePercentage =
        FailurePercentage(25f)
      implicit val customerCache: Cache[CustomerRequest, Customer, IO] =
        Cache.inMemory[CustomerRequest, Customer, IO]
      implicit val orderCache: Cache[OrderRequest, Order, IO] =
        Cache.inMemory[OrderRequest, Order, IO]
      val alg = Algebra[IO]
      val codecs = Codecs[IO]
      val sources = Sources[IO]
      val (metrics, cacheing, retries, chaos) =
        (Metrics[IO], Cacheing[IO], Retries[IO], Chaos[IO])
      val hardening = Hardening[IO](metrics, cacheing, retries, chaos)
      val getCustomer = hardening.hardened("getCustomer")(alg.getCustomer)
      val getOrder = hardening.hardened("getOrder")(alg.getOrder)
      val customerReqProcessor =
        Processor[IO, CustomerRequest, Option[Customer]](
          codecs.decodeCustomerReq,
          getCustomer,
          codecs.encodeCustomerResp
        )
      val orderReqProcessor = Processor[IO, OrderRequest, Option[Order]](
        codecs.decodeOrderReq,
        getOrder,
        codecs.encodeOrderResp
      )
      val pipeline =
        Pipeline[IO] {
          case EventType.CustomerRequest => customerReqProcessor
          case EventType.OrderRequest    => orderReqProcessor
        }
      val sinks = Sinks[IO]
      IODependencies(sources, pipeline, sinks)
    }
  }
}
