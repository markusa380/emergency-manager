import Dependencies._

ThisBuild / scalaVersion     := "2.13.1"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "com.github.markusa380"
ThisBuild / organizationName := "markusa380"
ThisBuild / resolvers        += "jitpack" at "https://jitpack.io"

lazy val assemble = taskKey[Unit]("Assembles the frontend in target")
lazy val copyAssetsToTarget = taskKey[Unit]("Copies the assets - directory to the target directory")
lazy val copyBundleToAssets = taskKey[Unit]("Copies the JavaScript - bundle to the assets folder in the target directory")

val jsBundleName = "frontend-fastopt-bundle.js"

val scalacOptionsList = Seq(
  "-encoding", "UTF-8",
  "-unchecked",
  "-deprecation",
  "-explaintypes",
  "-feature",
  "-language:higherKinds",
  "-Ywarn-extra-implicit",
  "-Ywarn-value-discard"
)

lazy val root = (project in file("."))
  .aggregate(frontend, backend)

lazy val common = (project in file("./common"))
  .settings(
    name := "Commons",
    scalacOptions ++= scalacOptionsList
  )

lazy val frontend = (project in file("./frontend"))
  .enablePlugins(
    ScalaJSPlugin,
    ScalaJSBundlerPlugin
  )
  .settings(
    name := "Frontend",
    scalacOptions ++= scalacOptionsList,
    Compile / unmanagedSourceDirectories ++= (common / Compile / unmanagedSourceDirectories).value,
    libraryDependencies ++= Seq(
      monix,
      cats,
      catsEffect,
      outwatch,
      circeCore,
      circeGeneric,
      circeParser,
      enumeratum
    )
    .map(_ withSources() withJavadoc()),

    // Config
    scalaJSUseMainModuleInitializer := true,
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    version in webpack := "4.41.5",
    emitSourceMaps := false,
    useYarn := true,

    copyAssetsToTarget := {
      println("Copying assets folder to target...")
      val mainVersion = scalaVersion.value.split("""\.""").take(2).mkString(".")
      val from = baseDirectory.value / "assets"
      val to = target.value / ("scala-" + mainVersion) / "assets"
      to.mkdirs()
      IO.copyDirectory(from, to)
      println("Open the following file in the web browser: " + (to / "index.html"))
    },

    copyBundleToAssets := {
      println("Copying JavaScript - bundle to target assets folder...")
      val mainVersion = scalaVersion.value.split("""\.""").take(2).mkString(".")
      val from = target.value / ("scala-" + mainVersion) / "scalajs-bundler" / "main" / jsBundleName
      val to = target.value / ("scala-" + mainVersion) / "assets" / "js" / jsBundleName
      IO.copyFile(from, to)
    },

    // Tasks
    assemble := {
      Def.sequential(
        Compile / fastOptJS / webpack,
        copyAssetsToTarget,
        copyBundleToAssets
      ).value
    }
  )

lazy val backend = (project in file("./backend"))
  .settings(
    name := "Backend",
    libraryDependencies ++= Seq(
      cats,
      catsEffect,
      http4sDsl,
      http4sBlazeServer,
      http4sBlazeClient,
      http4sCirce,
      circeCore,
      circeGeneric,
      shapeless,
      dynamoDb,
      scalaTest
    ),
    scalacOptions ++= scalacOptionsList
  )
