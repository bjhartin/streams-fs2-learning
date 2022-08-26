package streams

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  /*
    TODO:

      - Dependency injection
      - Resources
      - Config
      - Concurrency
      - Hardening
      - Algebra impl
      - Buffering

   */
  override def run(args: List[String]): IO[ExitCode] = {
    fs2
      .Stream(
        Sources.fileSources.customerRequests
          .through(Pipes.customerReqPipe),
        Sources.eventSources.orderRequests
          .through(Pipes.orderReqPipe)
      )
      .parJoin(maxOpen = 10)
      .attempt
      .compile
      .drain
      .map(_ => ExitCode.Success)
  }
}
