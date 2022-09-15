package streams.impl.io

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import streams.Refinements._
import streams.config.Configuration
import streams.{Pipeline, Sinks, Sources}

object IOMain extends IOApp {
  // Can't do anything about the UnsafeString (String here since this signature is dictated to us.
  // However, first step could be to refine those strings.
  override def run(args: List[UnsafeString]): IO[ExitCode] =
    for {
      _ <- refineArgs(args)
      _ <- Configuration.load // Don't need config yet.
      dependencies <- IODependencies.wireDependencies
      stream <- createStream(
        dependencies.routing,
        dependencies.sinks,
        dependencies.sources
      )
    } yield {
      stream
    }

  // Fails on the first argument that fails refinement.  Could do better.
  private def refineArgs(args: List[UnsafeString]): IO[List[SafeString]] =
    args
      .map(refineStringF[Predicates.SafeString, IO])
      .sequence

  // Creates a single stream
  private def createStream(
      pipeline: Pipeline[IO],
      sinks: Sinks[IO],
      sources: Sources[IO]
  ): IO[ExitCode] = {
    fs2
      .Stream(
        sources.events
          .through(pipeline.routeAndProcess)
          .through(sinks.responses)
      )
      .parJoin(maxOpen = 10)
      .attempt
      .compile
      .last
      .map {
        case Some(Left(err)) => throw err
        case _               => ExitCode.Success
      }
  }
}
