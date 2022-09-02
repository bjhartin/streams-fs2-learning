package streams

import cats.effect.IO
import org.scalatest.{OneInstancePerTest, funspec}

import scala.concurrent.Future

class AnyFunSpec extends funspec.AnyFunSpec with OneInstancePerTest {
  val BOOM = new RuntimeException("BOOM")
}

class AsyncFunSpec extends funspec.AsyncFunSpec with OneInstancePerTest {
  import TestRuntime._
  implicit def ioToFuture[A](io: IO[A]): Future[A] = io.unsafeToFuture()
  val BOOM = new RuntimeException("BOOM")
}
