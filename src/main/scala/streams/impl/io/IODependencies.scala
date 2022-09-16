package streams.impl.io

import cats.effect.IO
import nl.grons.metrics4.scala.MetricBuilder
import streams.Event.EventType
import streams.Refinements.{Name, Predicates, refineF, refineStringF}
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
import streams.http.HttpServer

object IODependencies {
  case class IODependencies(
      sources: Sources[IO],
      routing: Pipeline[IO],
      sinks: Sinks[IO],
      httpServer: fs2.Stream[IO, Unit]
  )

  def wireDependencies: IO[IODependencies] = {
    for {
      rawPercentage <- refineF[Float, Predicates.Percentage, IO](25f)
      failurePercentage = FailurePercentage(rawPercentage)
      getCustomerLabel <- refineStringF[Predicates.Name, IO]("getCustomer")
      getOrderLabel <- refineStringF[Predicates.Name, IO]("getOrder")
      deps <- buildDeps(failurePercentage, getCustomerLabel, getOrderLabel)
    } yield deps
  }

  def buildDeps(
      fp: FailurePercentage,
      getCustomerLabel: Name,
      getOrderLabel: Name
  ): IO[IODependencies] =
    IO.delay {
      import RetryConfig.default
      implicit val metricsBuilder: MetricBuilder = Metrics.metricsBuilder
      implicit val failurePercentage: FailurePercentage = fp
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
      val getCustomer = hardening.hardened(getCustomerLabel)(alg.getCustomer)
      val getOrder = hardening.hardened(getOrderLabel)(alg.getOrder)
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

      IODependencies(sources, pipeline, sinks, HttpServer().stream)
    }
}
