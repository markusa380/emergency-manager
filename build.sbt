import Dependencies._

import org.scalajs.sbtplugin.ScalaJSCrossVersion

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion     := "2.13.1"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "com.github.markusa380"
ThisBuild / organizationName := "markusa380"

lazy val pack = taskKey[Unit]("Packs all frontend resources in target")
lazy val copyAssetsToTarget = taskKey[Unit]("Copies the assets - directory to the target directory")
lazy val copyBundleToAssets = taskKey[Unit]("Copies the JavaScript - bundle to the assets folder in the target directory")

val jsBundleName = "frontend-fastopt-bundle.js"

val scalacOptionsList = Seq(
  "-encoding", "UTF-8",
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-explaintypes", // Explain type errors in more detail.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros", // Allow macro definition (besides implementation and application)
  "-language:higherKinds", // Allow higher-kinded types
  "-language:implicitConversions", // Allow definition of implicit functions called views
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
  // "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
  "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
  "-Xlint:option-implicit", // Option.apply used implicit view.
  "-Xlint:package-object-classes", // Class or object defined in package object.
  "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
  "-Ywarn-numeric-widen", // Warn when numerics are widened.
  "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
  "-Ywarn-unused:locals", // Warn if a local definition is unused.
  "-Ywarn-unused:params", // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates", // Warn if a private member is unused.
  "-Ywarn-value-discard", // Warn when non-Unit expression results are unused.
  "-Ybackend-parallelism", "8", // Enable paralellisation — change to desired number!
  "-Ycache-plugin-class-loader:last-modified", // Enables caching of classloaders for compiler plugins
  "-Ycache-macro-class-loader:last-modified", // and macro definitions. This can lead to performance improvements.
  // "-Xlog-implicits"
)

lazy val root = (project in file("."))
  .aggregate(common.jvm, common.js, frontend, backend)

lazy val common = (CrossPlugin.autoImport.crossProject(JSPlatform, JVMPlatform) in file("./common"))
  .settings(
    name := "Commons",
    libraryDependencies ++= Seq(
      cats,
      shapeless,
      scalaTest
    ),
    scalacOptions := scalacOptionsList
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
      circeShapes,
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
      circeShapes,
      shapeless,
      dynamoDb,
      scalaTest
    )
    .map(_ withSources() withJavadoc()),
    scalacOptions ++= scalacOptionsList,
    assemblyJarName in assembly := "backend.jar",
    assemblyMergeStrategy in assembly := {
      case a if a.contains("io.netty.versions.properties") => MergeStrategy.discard
      case a if a.contains("module-info.class") => MergeStrategy.discard
      case a =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(a)
    }
  )
