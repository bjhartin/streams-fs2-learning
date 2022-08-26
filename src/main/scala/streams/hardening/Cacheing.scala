package streams.hardening

object Cacheing {
  trait CacheClient[F[_]]

  def cached[A, B, F[_]: CacheClient](onCacheMiss: A => F[B]): A => F[B] = ???
}
