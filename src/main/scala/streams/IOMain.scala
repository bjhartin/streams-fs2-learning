package streams

import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp}
import streams.Refinements.{ArgumentRestrictions, UnsafeString, refineStringF}
import streams.config.Configuration

object IOMain extends IOApp {

  // Can't do anything about the String here since this signature is dictated to us.
  // However, first step could be to refine those strings.
  override def run(args: List[UnsafeString]): IO[ExitCode] =
    for {
      _ <-
        args
          .map(refineStringF[ArgumentRestrictions, IO])
          .sequence // Only reports first error - fix this.
      _ <- Configuration.load
      pipes <- IODependencies.wireDependencies
      stream <- createStream(pipes)
    } yield {
      stream
    }

  private def createStream(pipes: Pipes[IO]): IO[ExitCode] =
    fs2
      .Stream(
        Sources.fileSources.customerRequests
          .through(pipes.customerReqPipe),
        Sources.eventSources.orderRequests
          .through(pipes.orderReqPipe)
      )
      .parJoin(maxOpen = 10)
      .attempt
      .compile
      .drain
      .map(_ => ExitCode.Success)
}
