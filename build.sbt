val circeVersion = "0.14.1"
lazy val streamsFs2Learning =
  (project in file("."))
    .settings(
      scalaVersion := "2.13.8",
      libraryDependencies ++=
        Seq(
          "co.fs2" %% "fs2-core" % "3.2.7",
          "co.fs2" %% "fs2-io" % "3.2.7",
          "org.streams.scalacheck" %% "streams/scalacheck" % "1.14.1", // % "test" (I want this on the main classpath - not the usual)
          "com.github.alexarchambault" %% "streams.scalacheck-shapeless_1.15" % "1.3.0",
          "io.circe" %% "circe-core" % circeVersion,
          "io.circe" %% "circe-generic" % circeVersion,
          "io.circe" %% "circe-generic-extras" % circeVersion,
          "io.circe" %% "circe-parser" % circeVersion,
          "io.dropwizard.metrics" %% "metrics-core" % "4.2.0",
          "eu.timepit" %% "refined" % "0.10.1",
          "eu.timepit" %% "refined-cats" % "0.10.1"
        )
    )
