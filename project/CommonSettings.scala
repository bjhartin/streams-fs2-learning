import sbt.librarymanagement.syntax.Compile

object CommonSettings {
  import sbt.Keys._

  val projectSettings = Seq(
    organization := "com.deere.streaming_data",
    name := "sbt-core"
  )

  lazy val javaSettings = Seq(
    run / javaOptions ++= Seq("-encoding", "UTF-8"),
    Compile / javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")
  )

  lazy val scalaSettings = Seq(
    scalaVersion := "2.13.8"
  )
}
