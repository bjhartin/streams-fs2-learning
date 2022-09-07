package streams

import java.net.URL
import java.util.concurrent.TimeUnit

import cats.implicits._
import cats.effect.Temporal
import domain.Models.Messages.{CustomerRequest, OrderRequest}
import fs2.{Stream, text}
import fs2.io.file.{Files, Path}
import io.circe.Encoder
import org.scalacheck.Arbitrary
import streams.Refinements.UnsafeString

import scala.concurrent.duration.FiniteDuration

/*
  Here is where I'm defining the source of events that the program must process.
  Currently they are faked, but I think eventually this would be where we organize streams which originate
  via SQS, etc.  For HTTP (via HTTP4S), I'm not quite sure if it would make sense to put those here since
  HTTP4s has its own way.  Hopefully they could be listed here.  Assuming we had to write at least some
  plumbing, e.g. a generic SQS stream, that might not be in this file, but we would still organize
  the sources here for clarity.  At least that's my current thought, because I think there's value in
  being able to see the 'outside world' function calls at a glance.
 */
trait Sources[F[_]] {
  def customerRequests: Stream[F, UnsafeString]
  def orderRequests: Stream[F, UnsafeString]
}

object Sources {
  def eventSources[F[_]: Temporal]: Sources[F] =
    new Sources[F] {
      def customerRequests: Stream[F, UnsafeString] =
        phonyStream[CustomerRequest, F](30)
      def orderRequests: Stream[F, UnsafeString] =
        phonyStream[OrderRequest, F](10)
    }

  def fileSources[F[_]: Temporal: Files]: Sources[F] =
    new Sources[F] {
      case class MissingResourceException(path: UnsafeString)
          extends RuntimeException(s"Missing resource $path")
      def customerRequests: Stream[F, UnsafeString] = {
        Stream
          .eval(getPathToResource("fileSources/CustomerRequests.json"))
          .flatMap(Files[F].readAll)
          .through(text.utf8.decode)
          .through(text.lines)
      }

      override def orderRequests: Stream[F, UnsafeString] = {
        Stream
          .eval(getPathToResource("fileSources/OrderRequests.json"))
          .flatMap(Files[F].readAll)
          .through(text.utf8.decode)
          .through(text.lines)
      }

      private def getPathToResource(resourcePath: UnsafeString): F[Path] = {
        // Java API returns Null here if file missing.
        val x: F[URL] = Temporal[F]
          .fromOption(
            Option(getClass.getResource(resourcePath)),
            MissingResourceException(resourcePath)
          )
        x.map(r => Path(r.getPath.replaceAll("^/", "")))
      }
    }
  /*
    Randomly spit out a serialized msg from the outside world.
    FS2 has helper methods to do this kind of thing, but I wanted to work from scratch.
    You can see how this could poll a queue, etc.
   */
  private def phonyStream[A, F[_]: Temporal](
      maxMillisecondsBetweenValues: Long
  )(implicit
      enc: Encoder[A],
      arb: Arbitrary[A]
  ): Stream[F, UnsafeString] = {
    def next: Stream[F, UnsafeString] = {
      val req: F[UnsafeString] = for {
        t <- Temporal[F].pure(
          (Math.random() * maxMillisecondsBetweenValues).toLong
        )
        _ <- Temporal[F].sleep(FiniteDuration(t, TimeUnit.MILLISECONDS))
        _ <-
          if (System.currentTimeMillis() % 100 == 0)
            Temporal[F].raiseError(new RuntimeException("BOOM"))
          else Temporal[F].pure(())
        a <- Temporal[F].fromOption(
          arb.arbitrary.sample,
          new RuntimeException("Could not generate")
        )
      } yield {
        enc(a).noSpaces
      }

      Stream.eval(req) ++ next
    }
    next
  }
}
