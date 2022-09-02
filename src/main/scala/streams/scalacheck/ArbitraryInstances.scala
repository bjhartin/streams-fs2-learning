package streams.scalacheck

import org.scalacheck.{Arbitrary, Gen}
import streams.Refinements.UnsafeString

object ArbitraryInstances {
  implicit lazy val arbString: Arbitrary[UnsafeString] = Arbitrary(
    Gen.alphaNumStr
  )
}
