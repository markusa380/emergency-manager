package emergencymanager.backend.programs.controller

import cats.implicits._
import cats.effect._

import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.generic.semiauto._

import org.http4s.HttpRoutes
import org.http4s.syntax._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.circe._
import org.http4s.Request
import org.http4s.Response
import org.http4s.EntityDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.EntityEncoder
import cats.data.NonEmptyList
import org.http4s.Challenge
import java.io.File

import scala.io._
import org.http4s.headers._
import org.http4s.MediaType

object FrontendController {

    //implicit def jsonDecoder[A: Decoder]: EntityDecoder[IO, A] = CirceEntityDecoder.circeEntityDecoder
    //implicit def jsonEncoder[A: Encoder]: EntityEncoder[IO, A] = CirceEntityEncoder.circeEntityEncoder

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
        Source.fromFile(new File(path)).mkString
    }
}