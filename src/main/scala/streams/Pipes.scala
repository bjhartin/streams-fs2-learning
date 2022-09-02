package streams

import cats.effect.Sync
import fs2.Pipe
import streams.Refinements.UnsafeString

trait Pipes[F[_]] {
  def orderReqPipe: Pipe[F, UnsafeString, UnsafeString]
  def customerReqPipe: Pipe[F, UnsafeString, UnsafeString]
}

/*
  Pipes encapsulate the idea that events come in, get decoded, processed, encoded and sent out.

  I think this is analagous to a controller in MVC.
  For now, I think each pipe should have three elements: decode, process, encode.
  I think business logic should be in the processing functions.
 */
object Pipes {
  def apply[F[_]: Sync](
      processing: Processing[F],
      codecs: Codecs[F]
  ): Pipes[F] =
    new Pipes[F] {
      def orderReqPipe: Pipe[F, UnsafeString, UnsafeString] = { stream =>
        stream
          .through(codecs.decodeOrderReq)
          .evalTap(println)
          .through(processing.processOrderRequest)
          .through(codecs.encodeOrder)
      }

      //  // Doing it without pipes.
      //  def orderReqPipeline2(stream: fs2.Stream[IO, UnsafeString]): fs2.Stream[IO, Int] =
      //    stream
      //      .evalMap { s => IO(s.length) }
      //      .evalMap { i => IO(i + 1) }

      def customerReqPipe: Pipe[F, UnsafeString, UnsafeString] = { stream =>
        stream
          .evalTap(println)
          .through(codecs.decodeCustomerReq)
          .evalTap(println)
          .through(processing.processCustomerRequest)
          .evalTap(println)
          .through(codecs.encodeCustomer)
          .evalTap(println)
      }

      private def println[A](a: A): F[Unit] =
        Sync[F].delay(System.out.println(a))
    }

}
