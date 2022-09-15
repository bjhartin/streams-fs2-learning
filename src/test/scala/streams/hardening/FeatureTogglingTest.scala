package streams.hardening

import cats.effect.IO
import streams.AsyncFunSpec
import streams.hardening.FeatureToggling.FeatureToggles
import streams.hardening.FeatureToggling.FeatureToggles._

class FeatureTogglingTest extends AsyncFunSpec {
  import eu.timepit.refined.auto._
  it("should wrap a function with feature toggling") {
    val f1 = { s: String => IO(s.length) }
    val f2 = { s: String => IO(s.drop(1).length) }
    def toggles(result: FeatureToggleResult): FeatureToggles[IO] =
      _ => IO(result)
    def toggled(result: FeatureToggleResult): String => IO[Int] =
      FeatureToggling[IO].toggled("x")(f1)(f2)(toggles(result))

    for {
      r1 <- toggled(ToggledOn)("aa")
      r2 <- toggled(ToggledOff)("aa")
    } yield {
      assert(r1 == 2 && r2 == 1)
    }
  }
}
