package streams

import cats.ApplicativeThrow
import cats.implicits._
import cats.effect.Sync
import eu.timepit.refined._
import eu.timepit.refined.api.{Refined, Validate}
import eu.timepit.refined.boolean.And
import eu.timepit.refined.collection.{MaxSize, NonEmpty}
import eu.timepit.refined.string.MatchesRegex

// Refinement types.  Not sure if these should go here or somewhere else.
object Refinements {
  type UnsafeString = String
  type AlphaNumeric = MatchesRegex[W.`"[a-zA-Z0-9_-]+"`.T]
  type AlphaNumericNonEmpty = AlphaNumeric And NonEmpty
  type AlphaNumericNonEmptyMaxSize[N] =
    AlphaNumericNonEmpty And MaxSize[N] //Size[Interval.Closed[_0, N]]
  type NameRestrictions = AlphaNumericNonEmptyMaxSize[50]
  type ErrorMessageRestrictions = AlphaNumericNonEmptyMaxSize[100]
  // Probably needs enhanced.
  type EmailRestrictions = NonEmpty And MaxSize[100] And MatchesRegex[
    W.`"""[A-Za-z0-9]+@[A-Za-z0-9]+\\.[A-Za-z0-9]{2,3}"""`.T
  ]
  type SKURestrictions = MatchesRegex[
    W.`"""[A-Z0-9]{20}"""`.T
  ]
  type ArgumentRestrictions = AlphaNumericNonEmptyMaxSize[25]
  type JsonMessageRestrictions =
    AlphaNumericJSON And MaxSize[
      1000
    ] And NonEmpty // Would need expanding in real world
  type AlphaNumericJSON =
    MatchesRegex[W.`"""[\\{\\}\\[\\]\\" \\t\\n,=a-zA-Z0-9_]+"""`.T]
  type JsonMessageString = UnsafeString Refined JsonMessageRestrictions
  type Email = UnsafeString Refined EmailRestrictions
  type Name = UnsafeString Refined NameRestrictions
  type ErrorMessage = UnsafeString Refined ErrorMessageRestrictions
  type SKU = UnsafeString Refined SKURestrictions

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
