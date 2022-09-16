package streams.config

import cats.effect.IO
import cats.implicits._
import eu.timepit.refined.pureconfig._
import pureconfig._
import pureconfig.generic.semiauto._
import pureconfig.error.ConfigReaderFailures
import streams.Refinements.Name

case class Configuration(appname: Name)
object Configuration {

  implicit val configReader: ConfigReader[Configuration] =
    deriveReader[Configuration]

  case class ConfigurationFailure(failures: ConfigReaderFailures)
      extends RuntimeException(failures.prettyPrint())

  def load: IO[Configuration] =
    IO.fromEither(
      ConfigSource.default.load[Configuration].leftMap(ConfigurationFailure)
    )
}
