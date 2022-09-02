package streams

import java.util.concurrent.TimeUnit

import cats.effect.IO
import domain.Models.Messages.{CustomerRequest, OrderRequest}
import fs2.{Stream, text}
import fs2.io.file.{Files, Path}
import io.circe.Encoder
import org.scalacheck.Arbitrary
import streams.Refinements.UnsafeString

import scala.concurrent.duration.FiniteDuration

object Sources {
  object eventSources {
    def customerRequests: Stream[IO, UnsafeString] =
      phonyStream[CustomerRequest](30)
    def orderRequests: Stream[IO, UnsafeString] = phonyStream[OrderRequest](10)
  }

  object fileSources {
    def customerRequests: Stream[IO, UnsafeString] = {
      // TODO: How to handle error where file is missing?
      Files[IO]
        .readAll(getPathToResource("fileSources/CustomerRequests.json"))
        .through(text.utf8.decode)
        .through(text.lines)
    }

    private def getPathToResource(resourcePath: UnsafeString): Path = {
      val customerRequestFile = getClass
        .getResource(resourcePath)
        .getPath
        .replaceAll("^/", "")

      Path(customerRequestFile)
    }
  }
  /*
          Randomly spit out a serialized msg from the outside world.
          FS2 has helper methods to do this kind of thing, but I wanted to work from scratch.
          You can see how this could poll a queue, etc.
   */
  private def phonyStream[A](maxMillisecondsBetweenValues: Long)(implicit
      enc: Encoder[A],
      arb: Arbitrary[A]
  ): Stream[IO, UnsafeString] = {
    def next: Stream[IO, UnsafeString] = {
      val req: IO[UnsafeString] = for {
        t <- IO((Math.random() * maxMillisecondsBetweenValues).toLong)
        _ <- IO.sleep(FiniteDuration(t, TimeUnit.MILLISECONDS))
        _ <-
          if (System.currentTimeMillis() % 100 == 0)
            IO.raiseError(new RuntimeException("BOOM"))
          else IO(())
        a <- IO.fromOption(arb.arbitrary.sample)(
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
