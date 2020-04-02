package emergencymanager.backend.programs.controller

import cats.effect._

import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.headers._
import org.http4s.MediaType

import java.io.File

object FrontendController {

    val execDir = System.getProperty("user.dir")

    val httpRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
        case GET -> Root / "index.html" => load("index.html").flatMap(
            data => Ok(data, `Content-Type`(MediaType.text.html))
        )

        case GET -> Root / "js" / file => load(s"js/$file").flatMap(
            data => Ok(data, `Content-Type`(MediaType.application.javascript))
        )
    }

    def load(file: String): IO[String] = IO {
        val path = s"$execDir/../frontend/target/scala-2.13/assets/$file"
        println(path)
        scala.io.Source.fromFile(new File(path)).mkString
    }
}