package streams.hardening

import cats.effect.Clock
import cats.effect.kernel.Sync
import cats.implicits._
import com.codahale.metrics.Timer
import nl.grons.metrics4.scala.{DefaultInstrumented, MetricBuilder, MetricName}
import streams.Refinements

import scala.jdk.CollectionConverters._

object Metrics {
  import Refinements._

  case class NoSuchMetricException(
      label: Name
  ) extends RuntimeException(label)

  def metricsBuilder: MetricBuilder =
    new DefaultInstrumented {
      override lazy val metricBaseName: MetricName = MetricName("")
    }.metrics
}

// Effectful due to interacting with mutable Java API for metrics.
class Metrics[F[_]: Sync](implicit metrics: MetricBuilder) {
  import Metrics._
  import streams.Refinements._
  import metrics._

  // Fails if not found
  def getTimer(label: Name): F[Timer] =
    for {
      maybeMetric <-
        Sync[F].delay(metrics.registry.getTimers.asScala.get(label))
      metric <- Sync[F].fromOption(maybeMetric, NoSuchMetricException(label))
    } yield metric

  def timed[A, B](label: Name)(
      f: A => F[B]
  ): A => F[B] = { a: A =>
    for {
      pair <- Clock[F].timed(f(a))
      (duration, result) = pair
      _ <- Sync[F].delay {
        timer(label).update(duration)
      }
    } yield result
  }
}
