package streams.hardening

object Retries {
  trait RetryConfig[F[_]]
  def retried[A, B, F[_]: RetryConfig](f: A => F[B]): A => F[B] = ???
}
