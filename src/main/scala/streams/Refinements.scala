package streams

import cats.ApplicativeThrow
import cats.implicits._
import cats.effect.Sync
import eu.timepit.refined._
import eu.timepit.refined.api.{Refined, Validate}
import eu.timepit.refined.boolean.And
import eu.timepit.refined.collection.{MaxSize, NonEmpty}
import eu.timepit.refined.string.MatchesRegex

// Refinement types so we don't use unsafe types like String or unbounded lists, etc.
// The main idea is that many attacks rely on injecting executable code fragments into Strings or
// causing memory problems with oversize collectins.  Rather than sanitize input at various points,
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
  }

  type UnsafeString = String
  type Email = UnsafeString Refined Predicates.Email
  type Name = UnsafeString Refined Predicates.Name
  type SKU = UnsafeString Refined Predicates.SKU
  type SafeString = UnsafeString Refined Predicates.SafeString

  case class RefinementException(msg: UnsafeString)
      extends RuntimeException(msg)

  def refine[A, B](
      value: A
  )(implicit ev: Validate[A, B]): Either[RefinementException, A Refined B] =
    refineV[B](value)
      .leftMap(RefinementException)

  def refineF[A, B, F[_]: Sync](
      value: A
  )(implicit ev: Validate[A, B]): F[A Refined B] =
    Sync[F].fromEither(refine[A, B](value))

  def refineStringF[B, F[_]: ApplicativeThrow](
      value: UnsafeString
  )(implicit ev: Validate[UnsafeString, B]): F[UnsafeString Refined B] =
    ApplicativeThrow[F].fromEither(refine[UnsafeString, B](value))

  implicit def refinedStringToString[A](
      s: UnsafeString Refined A
  ): UnsafeString =
    s.value

  object Scalacheck {
    import org.scalacheck.{Arbitrary, Gen}
    import wolfendale.scalacheck.regexp.RegexpGen

    // Unfortunately we have to duplicate the constraints above when we generate values through ScalaCheck.
    // For simple cases, e.g. String Refined NonEmpty, this isn't the case, but for MatchesRegexp or MaxSize, we do.

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

    // I'm not sure if this can be made safe.  Gen's .sample method returns Option,
    // so it seems like it should be possible.
    private def unsafeGenRegexp[A](
        regexp: UnsafeString
    )(implicit ev: Validate[UnsafeString, A]): Gen[UnsafeString Refined A] =
      RegexpGen.from(regexp).map { v =>
        refine[UnsafeString, A](v).toOption.get
      }
  }
}
