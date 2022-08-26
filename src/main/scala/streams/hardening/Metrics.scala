package streams.hardening

object Metrics {
  case class Label(value: String) extends AnyVal
  trait MetricCollector[F[_]]
  def metered[A, B, F[_]: MetricCollector](label: Label)(
      f: A => F[B]
  ): A => F[B] = ???
  def counted[A, B, F[_]: MetricCollector](label: Label)(
      f: A => F[B]
  ): A => F[B] = ???
}
