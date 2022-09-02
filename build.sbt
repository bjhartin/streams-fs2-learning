addCompilerPlugin("io.tryp" % "splain" % "1.0.1" cross CrossVersion.patch)

val circeVersion = "0.14.1"
lazy val streamsFs2Learning =
  (project in file("."))
    .settings(
      CommonSettings.projectSettings,
      CommonSettings.javaSettings,
      CommonSettings.scalaSettings,
      libraryDependencies ++=
        Seq(
          "co.fs2" %% "fs2-core" % "3.2.12",
          "co.fs2" %% "fs2-io" % "3.2.12",
          "io.circe" %% "circe-core" % circeVersion,
          "io.circe" %% "circe-generic" % circeVersion,
          "io.circe" %% "circe-generic-extras" % circeVersion,
          "io.circe" %% "circe-refined" % circeVersion,
          "io.circe" %% "circe-parser" % circeVersion,
          "nl.grons" %% "metrics4-scala" % "4.2.9",
          "eu.timepit" %% "refined" % "0.10.1",
          "eu.timepit" %% "refined-cats" % "0.10.1",
          "eu.timepit" %% "refined-pureconfig" % "0.10.1",
          "eu.timepit" %% "refined-scalacheck" % "0.10.1",
          "org.scalatest" %% "scalatest" % "3.2.13",
          "org.scalacheck" %% "scalacheck" % "1.14.1", // % "test" (I want these on the main classpath temporarily - not the usual)
          "com.github.pureconfig" %% "pureconfig" % "0.17.1",
          "io.github.wolfendale" %% "scalacheck-gen-regexp" % "0.1.3",
          "com.github.alexarchambault" %% "scalacheck-shapeless_1.16" % "1.3.1"
        )
    )
