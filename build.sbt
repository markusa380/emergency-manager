import Dependencies._

ThisBuild / scalaVersion     := "2.13.1"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "com.github.markusa380"
ThisBuild / organizationName := "markusa380"

val http4sVersion = "0.21.0"

lazy val root = (project in file("."))
  .settings(
    name := "Emergency Manager Backend",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.1.1",
      "org.typelevel" %% "cats-effect" % "2.1.0",
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion,
      "com.chuusai" %% "shapeless" % "2.3.3",
      "software.amazon.awssdk" % "dynamodb" % "2.10.74",
      "org.scalatest" %% "scalatest" % "3.1.1" % "test"
    ),
    scalacOptions ++= Seq(
      "-encoding", "UTF-8",
      "-unchecked",
      "-deprecation",
      "-explaintypes",
      "-feature",
      "-language:higherKinds",
      "-Ywarn-extra-implicit",
      "-Ywarn-value-discard"
    )
  )
