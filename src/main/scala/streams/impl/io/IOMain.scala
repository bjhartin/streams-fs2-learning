package streams.impl.io

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import streams.Refinements._
import streams.config.Configuration
import streams.{Pipes, Sources}

object IOMain extends IOApp {
  // Can't do anything about the UnsafeString (String here since this signature is dictated to us.
  // However, first step could be to refine those strings.
  override def run(args: List[UnsafeString]): IO[ExitCode] =
    for {
      _ <- refineArgs(args) // Only reports first error - fix this.
      _ <- Configuration.load
      pipes <- IODependencies.wireDependencies
      stream <- createStream(pipes)
    } yield {
      stream
    }

  // Fails on the first argument that fails refinement.  Could do better.
  private def refineArgs(args: List[UnsafeString]): IO[List[SafeString]] =
    args
      .map(refineStringF[Predicates.SafeString, IO])
      .sequence

  // Creates a single stream
  private def createStream(pipes: Pipes[IO]): IO[ExitCode] = {
    fs2
      .Stream(
        Sources
          .fileSources[IO]
          .customerRequests
          .through(pipes.customerReqPipe),
        Sources
          .eventSources[IO]
          .orderRequests
          .through(pipes.orderReqPipe)
      )
      .parJoin(maxOpen = 10)
      .attempt
      .compile
      .drain
      .map(_ => ExitCode.Success)
  }
}
