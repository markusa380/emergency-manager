package emergencymanager.backend.controller

import cats.effect._

import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.headers._
import org.http4s.MediaType

import java.io.File
import java.nio.file._

object FrontendController {

    def httpRoutes(assetsDir: String): HttpRoutes[IO] = HttpRoutes.of[IO] {

        case GET -> Root => handleInternalError(
            loadText(assetsDir, "index.html").flatMap(
                data => Ok(data, `Content-Type`(MediaType.text.html))
            )
        )

        case GET -> Root / "js" / file => handleInternalError(
            loadText(assetsDir, s"js/$file").flatMap(
                data => Ok(data, `Content-Type`(MediaType.application.javascript))
            )
        )

        case GET -> Root / "favicon.ico" => handleInternalError(
            loadBinary(assetsDir, s"favicon.ico").flatMap(
                data => Ok(data, `Content-Type`(MediaType.image.`vnd.microsoft.icon`))
            )
        )
    }

    def loadText(assetsDir: String, file: String): IO[String] = IO {
        val path = assetsDir + File.separator + file
        scala.io.Source.fromFile(new File(path))("UTF-8").mkString
    }

    def loadBinary(assetsDir: String, file: String): IO[Array[Byte]] = IO {
        val path = assetsDir + File.separator + file
        Files.readAllBytes(Paths.get(path))
    }
}