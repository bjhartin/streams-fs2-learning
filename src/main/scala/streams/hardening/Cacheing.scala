package streams.hardening

import cats.implicits._
import cats.effect.Sync

// TODO: The CacheClient would be in terms of a Key and Value type (likely string or bytes)
// and not a specific A and B.  We'll need to ensure codecs are present.
trait Cache[A, B, F[_]] {
  def get(a: A): F[Option[B]]
  def put(a: A, b: B): F[Unit]
}

class Cacheing[F[_]: Sync] {
  def cached[A, B](
      onCacheMiss: A => F[Option[B]]
  )(implicit cacheClient: Cache[A, B, F]): A => F[Option[B]] = { a: A =>
    for {
      maybeCached <- cacheClient.get(a)
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
