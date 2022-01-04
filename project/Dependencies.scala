import sbt._

object Dependencies {

  lazy val http4sVersion = "0.21.0"
  lazy val circeVersion = "0.13.0"
  lazy val outwatchVersion = "efa1edfe53"
  lazy val fs2Version = "2.2.1"
  lazy val mongoEffectVersion = "0.1.0"
  lazy val mongoDriverVersion = "2.7.0"

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1" % "test"
  lazy val cats = "org.typelevel" %% "cats-core" % "2.7.0"
  lazy val catsEffect = "org.typelevel" %% "cats-effect" % "2.5.3"
  lazy val monix = "io.monix" %% "monix" % "3.4.0"
  lazy val http4sDsl = "org.http4s" %% "http4s-dsl" % http4sVersion
  lazy val http4sBlazeServer = "org.http4s" %% "http4s-blaze-server" % http4sVersion
  lazy val http4sBlazeClient = "org.http4s" %% "http4s-blaze-client" % http4sVersion
  lazy val http4sCirce = "org.http4s" %% "http4s-circe" % http4sVersion
  lazy val shapeless = "com.chuusai" %% "shapeless" % "2.3.3"
  lazy val dynamoDb = "software.amazon.awssdk" % "dynamodb" % "2.10.74"

  lazy val circeCore = "io.circe" %% "circe-core" % circeVersion
  lazy val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
  lazy val circeParser = "io.circe" %% "circe-parser" % circeVersion
  lazy val circeShapes = "io.circe" %% "circe-shapes" % circeVersion

  lazy val outwatch = "com.github.outwatch.outwatch" % "outwatch" % outwatchVersion
  lazy val outwatchUtil = "com.github.outwatch.outwatch" % "outwatch-util" % outwatchVersion

  lazy val enumeratum = "com.beachape" % "enumeratum" % "1.7.0"

  lazy val fs2Core = "co.fs2" %% "fs2-core" % fs2Version
  lazy val fs2React = "co.fs2" %% "fs2-reactive-streams" % fs2Version

  lazy val mongoDriver = "org.mongodb.scala" %% "mongo-scala-driver" % mongoDriverVersion

  lazy val pureConfig = "com.github.pureconfig" %% "pureconfig" % "0.12.3"
}
