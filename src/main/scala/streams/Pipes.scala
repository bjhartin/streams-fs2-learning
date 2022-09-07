package streams

import cats.effect.Sync
import fs2.Pipe
import streams.Refinements.UnsafeString

trait Pipes[F[_]] {
  def orderReqPipe: Pipe[F, UnsafeString, Unit]
  def customerReqPipe: Pipe[F, UnsafeString, Unit]
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
      codecs: Codecs[F],
      sinks: Sinks[F]
  ): Pipes[F] =
    new Pipes[F] {
      def orderReqPipe: Pipe[F, UnsafeString, Unit] = { stream =>
        stream
          .through(codecs.decodeOrderReq)
          .evalTap(println)
          .through(processing.processOrderRequest)
          .through(codecs.encodeOrderResp)
          .through(sinks.orderResponses)
      }

      //  // Doing it without pipes.
      //  def orderReqPipeline2(stream: fs2.Stream[IO, UnsafeString]): fs2.Stream[IO, Int] =
      //    stream
      //      .evalMap { s => IO(s.length) }
      //      .evalMap { i => IO(i + 1) }

      def customerReqPipe: Pipe[F, UnsafeString, Unit] = { stream =>
        stream
          .evalTap(println)
          .through(codecs.decodeCustomerReq)
          .evalTap(println)
          .through(processing.processCustomerRequest)
          .evalTap(println)
          .through(codecs.encodeCustomerResp)
          .evalTap(println)
          .through(sinks.customerResponses)
      }

      private def println[A](a: A): F[Unit] =
        Sync[F].delay(System.out.println(a))
    }

}
