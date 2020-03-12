package backend.programs

import backend.model.Supplies

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
import org.http4s.EntityDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.EntityEncoder

object SuppliesController {

    object NameQueryParamMatcher extends QueryParamDecoderMatcher[String]("name")


    implicit def jsonDecoder[A: Decoder]: EntityDecoder[IO, A] = CirceEntityDecoder.circeEntityDecoder
    implicit def jsonEncoder[A: Encoder]: EntityEncoder[IO, A] = CirceEntityEncoder.circeEntityEncoder

    def httpRoutes(supplies: SuppliesStorage): HttpRoutes[IO] = HttpRoutes.of[IO] {
        case GET -> Root / "supplies" => Ok(
            supplies.list
        )
        case GET -> Root / "supplies" / "calories" => Ok(
            supplies.sumCalories
        )
        case GET -> Root / "supplies" / "search" :? NameQueryParamMatcher(nameSearch) => Ok(
            supplies.findName(nameSearch)
        )
        case req @ POST -> Root / "supplies" => Ok(
            req.as[Supplies]
                .flatMap(supplies.createOrOverwrite)
        )
    }
}