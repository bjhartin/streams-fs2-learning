package streams

import cats.effect.IO
import fs2.Pipe

object Pipes {
  def orderReqPipe: Pipe[IO, String, String] = { stream =>
    stream
      .through(Codecs.decodeOrderReq)
      .evalTap(IO.println)
      .through(Processing.processOrderRequest)
      .through(Codecs.encodeOrder)
  }

  //  // Doing it without pipelines.
  //  def orderReqPipeline2(stream: fs2.Stream[IO, String]): fs2.Stream[IO, Int] =
  //    stream
  //      .evalMap { s => IO(s.length) }
  //      .evalMap { i => IO(i + 1) }

  def customerReqPipe: Pipe[IO, String, String] = { stream =>
    stream
      .evalTap(IO.println)
      .through(Codecs.decodeCustomerReq)
      .evalTap(IO.println)
      .through(Processing.processCustomerRequest)
      .evalTap(IO.println)
      .through(Codecs.encodeCustomer)
      .evalTap(IO.println)
  }
}
