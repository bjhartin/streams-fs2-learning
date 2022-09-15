package streams

import cats.implicits._
import cats.effect.Sync

// This is the abstraction for allowing the outside world to invoke some A => F[B]
// with the loosely typed data they have sent.
//
// It decodes the event content to an A, invokes A => F[B] and encodes from a B.
abstract class Processor[F[_]: Sync, A, B](
    decode: Event => F[A],
    process: A => F[B],
    encode: B => F[Response]
) {
  def apply(e: Event): F[Response] =
    for {
      _ <- printlnF(s"received a ${e.eventType} msg")
      _ <- printlnF(s"  content is ${e.content}")
      req <- decode(e)
      _ <- printlnF(s"    decoded $req")
      ord <- process(req)
      _ <- printlnF(s"     result $ord")
      resp <- encode(ord)
      _ <- printlnF(s"       encoded a ${resp.responseType}")
    } yield resp

  private def printlnF(s: String): F[Unit] =
    Sync[F].delay(System.out.println(s))
}

object Processor {
  def apply[F[_]: Sync, A, B](
      decode: Event => F[A],
      process: A => F[B],
      encode: B => F[Response]
  ): Processor[F, A, B] = new Processor[F, A, B](decode, process, encode) {}
}
