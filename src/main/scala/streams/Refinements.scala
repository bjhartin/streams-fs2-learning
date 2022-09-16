package streams

import cats.ApplicativeThrow
import cats.implicits._
import eu.timepit.refined._
import eu.timepit.refined.api.{Refined, Validate}
import eu.timepit.refined.boolean.And
import eu.timepit.refined.collection.{MaxSize, NonEmpty}
import eu.timepit.refined.numeric.{Less, NonNegative}
import eu.timepit.refined.string.MatchesRegex

// Refinement types so we don't use unsafe types like String or unbounded lists, etc.
// The main idea is that many attacks rely on injecting executable code fragments into Strings or
// causing memory problems with oversize collections.  Rather than sanitize input at various points,
// we'll use types incapable of representing those things.  We'll use scalastyle rules to prevent the use
// of the dangerous types in most places.
object Refinements {
  object Predicates {
    private type AlphaNumeric = MatchesRegex[W.`"[a-zA-Z0-9_-]+"`.T]
    // Best attempt at ensuring no code is injected.  We'll use more restrictive types when we can.
    type SafeString = MaxSize[50] And MatchesRegex[
      W.`"""[^\\(\\)\\{\\}\\[\\]\\!\\#\\$\\:\\;]"""`.T
    ]
    type Name = AlphaNumeric And NonEmpty And MaxSize[50]
    type Email = NonEmpty And MaxSize[100] And MatchesRegex[
      W.`"""[A-Za-z0-9]+@[A-Za-z0-9]+\\.[A-Za-z0-9]{2,3}"""`.T
    ] // Probably needs enhanced.
    type SKU = MatchesRegex[W.`"""[A-Z0-9]{20}"""`.T]
    type Percentage = NonNegative And Less[100]
    type Price = NonNegative And Less[10000]
  }

  type UnsafeString = String
  type Email = UnsafeString Refined Predicates.Email
  type Name = UnsafeString Refined Predicates.Name
  type SKU = UnsafeString Refined Predicates.SKU
  type Price = Float Refined Predicates.Price
  type SafeString = UnsafeString Refined Predicates.SafeString
  type Percentage = Float Refined Predicates.Percentage
  case class RefinementException(msg: UnsafeString)
      extends RuntimeException(msg)

  def refine[A, B](
      value: A
  )(implicit ev: Validate[A, B]): Either[RefinementException, A Refined B] =
    refineV[B](value)
      .leftMap(RefinementException)

  def refineString[A](value: String)(implicit
      ev: Validate[String, A]
  ): Either[RefinementException, String Refined A] =
    refine[String, A](value)

  def refineF[A, B, F[_]: ApplicativeThrow](
      value: A
  )(implicit ev: Validate[A, B]): F[A Refined B] =
    ApplicativeThrow[F].fromEither(refine[A, B](value))

  def refineStringF[A, F[_]: ApplicativeThrow](
      value: UnsafeString
  )(implicit ev: Validate[UnsafeString, A]): F[UnsafeString Refined A] =
    ApplicativeThrow[F].fromEither(refine[UnsafeString, A](value))

  implicit def refinedStringToString[A](
      s: UnsafeString Refined A
  ): UnsafeString =
    s.value

  object Scalacheck {
    import org.scalacheck.{Arbitrary, Gen}
    import wolfendale.scalacheck.regexp.RegexpGen

    // Unfortunately we have to duplicate the constraints above when we generate values through ScalaCheck.
    // For simple cases, e.g. String Refined NonEmpty, this isn't the case, but for MatchesRegexp or MaxSize, we do.
    // It might be possible to drive this from MatchesRegex.

    implicit lazy val unsafeArbName: Arbitrary[Name] = Arbitrary(
      unsafeGenRegexp("[a-zA-Z0-9_-]{1,50}")
    )

    implicit lazy val unsafeArbEmail: Arbitrary[Email] = Arbitrary(
      unsafeGenRegexp(
        """[A-Za-z0-9]{2,20}@[A-Za-z0-9]{2,20}\.[A-Za-z0-9]{2,3}"""
      )
    )

    implicit lazy val unsafeArbSku: Arbitrary[SKU] = Arbitrary(
      unsafeGenRegexp("""[A-Z0-9]{20}""")
    )

    implicit lazy val arbString: Arbitrary[UnsafeString] = Arbitrary(
      Gen.alphaNumStr
    )

    implicit val arbPrice: Arbitrary[Price] = Arbitrary(
      Gen.posNum[Float].map { f =>
        refine[Float, Predicates.Price](f).toTry.get
      }
    )

    // I'm not sure if this can be made safe.  Gen's .sample method returns Option,
    // so it seems like it should be possible, but so far I've not seen how to leverage
    // that when I have an Option.
    private def unsafeGenRegexp[A](
        regexp: UnsafeString
    )(implicit ev: Validate[UnsafeString, A]): Gen[UnsafeString Refined A] =
      RegexpGen.from(regexp).map { v =>
        refine[UnsafeString, A](v).toOption.get
      }
  }
}
