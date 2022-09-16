package streams.impl.io

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import streams.Refinements._
import streams.config.Configuration
import streams.{Pipeline, Sinks, Sources}

object IOMain extends IOApp {
  override def run(args: List[UnsafeString]): IO[ExitCode] = {
    for {
      _ <- refineArgs(args)
      _ <- Configuration.load // Don't need config yet.
      dependencies <- IODependencies.wireDependencies
      evtStream = eventHandlingStream(
        dependencies.routing,
        dependencies.sinks,
        dependencies.sources
      )
      httpStream = dependencies.httpServer
      combined =
        httpStream.merge(
          evtStream
        ) // Possibly not the best way to run two unrelated streams.
      result <- dispose(combined)
    } yield {
      result
    }
  }

  private def dispose(str: fs2.Stream[IO, Unit]): IO[ExitCode] = {
    str.attempt.compile.last
      .map {
        case Some(Left(err)) => throw err
        case _               => ExitCode.Success
      }
  }

  // Fails on the first argument that fails refinement.  Could do better.
  private def refineArgs(args: List[UnsafeString]): IO[List[SafeString]] =
    args
      .map(refineStringF[Predicates.SafeString, IO])
      .sequence

  private def eventHandlingStream(
      pipeline: Pipeline[IO],
      sinks: Sinks[IO],
      sources: Sources[IO]
  ): fs2.Stream[IO, Unit] = {
    fs2
      .Stream(
        sources.events
          .through(pipeline.routeAndProcess)
          .through(sinks.responses)
      )
      .parJoin(maxOpen = 10)
  }
}
