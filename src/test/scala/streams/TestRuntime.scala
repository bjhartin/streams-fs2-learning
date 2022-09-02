package streams

import cats.effect.unsafe.IORuntime

object TestRuntime {
  implicit val rt: IORuntime = IORuntime.global
}
