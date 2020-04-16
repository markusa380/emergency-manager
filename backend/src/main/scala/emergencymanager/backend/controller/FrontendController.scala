package emergencymanager.backend.controller

import cats.effect._

import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.headers._
import org.http4s.MediaType

import java.io.File

object FrontendController {

    def httpRoutes(assetsDir: String): HttpRoutes[IO] = HttpRoutes.of[IO] {

        case GET -> Root => handleInternalError(
            load(assetsDir, "index.html").flatMap(
                data => Ok(data, `Content-Type`(MediaType.text.html))
            )
        )

        case GET -> Root / "js" / file => handleInternalError(
            load(assetsDir, s"js/$file").flatMap(
                data => Ok(data, `Content-Type`(MediaType.application.javascript))
            )
        )
    }

    def load(assetsDir: String, file: String): IO[String] = IO {
        val path = assetsDir + File.separator + file
        scala.io.Source.fromFile(new File(path))("UTF-8").mkString
    }
}