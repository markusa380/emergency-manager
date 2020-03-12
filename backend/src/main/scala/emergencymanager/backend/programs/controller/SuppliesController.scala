package emergencymanager.backend.programs.controller

import emergencymanager.commons.data.Supplies
import emergencymanager.backend.data.EMSupplies

import emergencymanager.backend.programs.service.SuppliesService
import emergencymanager.backend.programs.service.UserService

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

object SuppliesController {

    object NameQueryParamMatcher extends QueryParamDecoderMatcher[String]("name")

    implicit def jsonDecoder[A: Decoder]: EntityDecoder[IO, A] = CirceEntityDecoder.circeEntityDecoder
    implicit def jsonEncoder[A: Encoder]: EntityEncoder[IO, A] = CirceEntityEncoder.circeEntityEncoder

    def httpRoutes(supplies: SuppliesService, users: UserService): HttpRoutes[IO] = HttpRoutes.of[IO] {
        case req @ GET -> Root / "api" / "supplies" => authenticate(users)(
            req,
            user => {
                println(user)
                Ok(supplies.list(user))
            }
        )
        .onError(t => IO(println(t.getMessage())))

        case req @ GET -> Root / "api" / "supplies" / "calories" => authenticate(users)(
            req,
            user => Ok(supplies.sumCalories(user))
        )

        case req @ GET -> Root / "api" / "supplies" / "search" :? NameQueryParamMatcher(nameSearch) => authenticate(users)(
            req,
            user => Ok(
                supplies.findName(nameSearch, user)
                    .map(
                        _.map(es =>
                            Supplies(es.id, es.name, es.bestBefore, es.kiloCalories, es.weightGrams, es.number)
                        )
                    )
            )
        )

        case req @ POST -> Root / "api" / "supplies" => authenticate(users)(
            req,
            user => Ok(
                req.as[Supplies]
                    .map(s => EMSupplies(s.id, s.name, user, s.bestBefore, s.kiloCalories, s.weightGrams, s.number))
                    .flatMap(supplies.createOrOverwrite)
            )
        )
    }
}