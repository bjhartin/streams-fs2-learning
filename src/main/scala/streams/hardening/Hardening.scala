package streams.hardening

object Hardening {
  import Cacheing._
  import Metrics._
  import Retries._

  def hardened[A, B, F[_]: RetryConfig: CacheClient: MetricCollector](
      label: Label
  )(f: A => F[B]): A => F[B] =
    metered(label.copy(value = s"${label.value} (cached)"))(
      cached(retried(metered(label)(f)))
    )
}
