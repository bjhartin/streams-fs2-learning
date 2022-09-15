package streams

import cats.effect.Sync

import scala.collection.concurrent.TrieMap

// TODO: The streams.Cache would be in terms of a Key and Value type (likely string or bytes)
// and not a specific A and B.  We'll need to ensure codecs are present.
trait Cache[A, B, F[_]] {
  def get(a: A): F[Option[B]]
  def put(a: A, b: B): F[Unit]
}

object Cache {
  // TODO: TTL
  def inMemory[A, B, F[_]: Sync]: Cache[A, B, F] =
    new Cache[A, B, F] {
      // Yes, it's mutable, but it's access is wrapped in effects, just like any other data store.
      private val inMemoryCache: TrieMap[A, B] = TrieMap.empty[A, B]
      override def get(a: A): F[Option[B]] =
        Sync[F].delay { inMemoryCache.get(a) }
      override def put(a: A, b: B): F[Unit] =
        Sync[F].delay { inMemoryCache.put(a, b); () }
    }
}
