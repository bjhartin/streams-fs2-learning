package streams.hardening

import cats.effect.IO
import streams.AsyncFunSpec
import streams.Refinements.{Name, Predicates, UnsafeString, refineStringF}
import streams.hardening.FeatureToggling.FeatureToggles
import streams.hardening.FeatureToggling.FeatureToggles._

class FeatureTogglingTest extends AsyncFunSpec {

  it("should wrap a function with feature toggling") {
    val f1 = { s: String => IO(s.length) }
    val f2 = { s: String => IO(s.drop(1).length) }
    def toggles(result: FeatureToggleResult): FeatureToggles[IO] =
      _ => IO(result)
    def toggled(
        name: Name,
        result: FeatureToggleResult
    ): UnsafeString => IO[Int] =
      FeatureToggling[IO].toggled(name)(f1)(f2)(toggles(result))

    for {
      name <- refineStringF[Predicates.Name, IO]("function")
      r1 <- toggled(name, ToggledOn)("aa")
      r2 <- toggled(name, ToggledOff)("aa")
    } yield {
      assert(r1 == 2 && r2 == 1)
    }
  }
}
