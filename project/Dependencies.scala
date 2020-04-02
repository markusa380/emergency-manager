import sbt._

object Dependencies {

  lazy val http4sVersion = "0.21.0"
  lazy val circeVersion = "0.12.3"
  lazy val outwatchVersion = "08bc0bf6af"

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1" % "test"
  lazy val cats = "org.typelevel" %% "cats-core" % "2.1.1"
  lazy val catsEffect = "org.typelevel" %% "cats-effect" % "2.1.0"
  lazy val monix = "io.monix" %% "monix" % "3.1.0"
  lazy val http4sDsl = "org.http4s" %% "http4s-dsl" % http4sVersion
  lazy val http4sBlazeServer = "org.http4s" %% "http4s-blaze-server" % http4sVersion
  lazy val http4sBlazeClient = "org.http4s" %% "http4s-blaze-client" % http4sVersion
  lazy val http4sCirce = "org.http4s" %% "http4s-circe" % http4sVersion
  lazy val shapeless = "com.chuusai" %% "shapeless" % "2.3.3"
  lazy val dynamoDb = "software.amazon.awssdk" % "dynamodb" % "2.10.74"
  lazy val circeCore = "io.circe" %% "circe-core" % circeVersion
  lazy val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
  lazy val circeParser = "io.circe" %% "circe-parser" % circeVersion

  lazy val outwatch = "com.github.outwatch.outwatch" % "outwatch" % outwatchVersion
  lazy val outwatchUtil = "com.github.outwatch.outwatch" % "outwatch-util" % outwatchVersion

  lazy val enumeratum = "com.beachape" % "enumeratum" % "1.5.15"
}
