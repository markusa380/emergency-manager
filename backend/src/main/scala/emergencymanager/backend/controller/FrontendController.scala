package emergencymanager.backend.controller

import cats.effect._

import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.headers._
import org.http4s.MediaType

import java.io.File

object FrontendController {

    val execDir = Option(System.getProperty("user.dir")).getOrElse("~")

    val httpRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {

        case GET -> Root => handleInternalError(
            load("index.html").flatMap(
                data => Ok(data, `Content-Type`(MediaType.text.html))
            )
        )

        case GET -> Root / "index.html" => handleInternalError(
            load("index.html").flatMap(
                data => Ok(data, `Content-Type`(MediaType.text.html))
            )
        )

        case GET -> Root / "js" / file => handleInternalError(
            load(s"js/$file").flatMap(
                data => Ok(data, `Content-Type`(MediaType.application.javascript))
            )
        )
    }

    def load(file: String): IO[String] = loadDir(file)
        .handleErrorWith(_ => loadDev(file))

    def loadDir(file: String): IO[String] = IO {
        val path = s"$execDir/$file"
        scala.io.Source.fromFile(new File(path)).mkString
    }

    def loadDev(file: String) = IO {
        val path = s"$execDir/../frontend/target/scala-2.13/assets/$file"
        scala.io.Source.fromFile(new File(path)).mkString
    }
}