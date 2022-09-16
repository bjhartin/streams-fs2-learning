package streams.http

import org.http4s.HttpRoutes
import cats.effect._
import com.comcast.ip4s.IpLiteralSyntax
import org.http4s.dsl.io._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.http4s.implicits._
import org.http4s.server.Router

trait HttpServer {
  def stream: fs2.Stream[IO, Unit]
}

object HttpServer {
  def apply(): HttpServer =
    new HttpServer {
      def stream: fs2.Stream[IO, Unit] =
        fs2.Stream.eval(build.use(_ => IO.never.as(())))
    }

  private def build: Resource[IO, Server] = {
    val healthRoutes = HttpRoutes.of[IO] {
      case GET -> Root / "check" =>
        Ok("")
    }
    val httpApp = Router("/health" -> healthRoutes).orNotFound
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(httpApp)
      .build
  }
}
