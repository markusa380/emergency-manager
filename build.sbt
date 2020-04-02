import Dependencies._

import org.scalajs.sbtplugin.ScalaJSCrossVersion

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion     := "2.13.1"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "com.github.markusa380"
ThisBuild / organizationName := "markusa380"

lazy val serve = taskKey[Unit]("Starts the server")
lazy val pack = taskKey[Unit]("Packs all frontend resources in target")
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

lazy val common = (CrossPlugin.autoImport.crossProject(JSPlatform, JVMPlatform) in file("./common"))
  .settings(
    name := "Commons"
  )
  .jvmSettings(
    // Add JVM-specific settings here
  )
  .jsSettings(
    // Add JS-specific settings here
    scalaJSUseMainModuleInitializer := true,
  )

lazy val frontend = (project in file("./frontend"))
  .dependsOn(common.js)
  .enablePlugins(
    ScalaJSPlugin,
    ScalaJSBundlerPlugin
  )
  .settings(
    name := "Frontend",
    scalacOptions ++= scalacOptionsList,
    resolvers += "jitpack" at "https://jitpack.io",
    libraryDependencies ++= Seq(
      cats,
      catsEffect,
      outwatch,
      outwatchUtil,
      circeCore ,
      circeGeneric,
      circeParser,
      enumeratum
    )
    .map(_ cross ScalaJSCrossVersion.binary withSources() withJavadoc()),

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
    pack := {
      Def.sequential(
        Compile / fastOptJS / webpack,
        copyAssetsToTarget,
        copyBundleToAssets
      ).value
    }
  )

lazy val backend = (project in file("./backend"))
  .dependsOn(common.jvm)
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
    )
    .map(_ withSources() withJavadoc()),
    scalacOptions ++= scalacOptionsList
  )
