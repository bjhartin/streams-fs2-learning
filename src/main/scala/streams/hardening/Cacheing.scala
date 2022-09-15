package streams.hardening

import cats.implicits._
import cats.effect.Sync
import streams.Cache

trait Cacheing[F[_]] {
  def cached[A, B](
      onCacheMiss: A => F[Option[B]]
  )(implicit cache: Cache[A, B, F]): A => F[Option[B]]
}

object Cacheing {
  def apply[F[_]: Sync]: Cacheing[F] =
    new Cacheing[F] {
      def cached[A, B](
          onCacheMiss: A => F[Option[B]]
      )(implicit cache: Cache[A, B, F]): A => F[Option[B]] = { a: A =>
        for {
          maybeCached <- cache.get(a)
          result <- maybeCached match {
            case None => handleCacheMiss(onCacheMiss, a)
            case some => Sync[F].pure(some)
          }
        } yield result
      }

      private def handleCacheMiss[A, B](onCacheMiss: A => F[Option[B]], a: A)(
          implicit cacheClient: Cache[A, B, F]
      ): F[Option[B]] =
        for {
          found <- onCacheMiss(a)
          result <- found match {
            case None        => Sync[F].pure(None)
            case s @ Some(v) => cacheClient.put(a, v).map(_ => s)
          }
        } yield result
    }
}
