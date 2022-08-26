package streams.scalacheck

import org.scalacheck.{Arbitrary, Gen}

object ArbitraryInstances {
  implicit lazy val arbString: Arbitrary[String] = Arbitrary(Gen.alphaNumStr)
}
