package emergencymanager.backend.programs.controller

import emergencymanager.commons.data._

import emergencymanager.backend.programs.service.SuppliesService
import emergencymanager.backend.programs.service.UserService

import cats.implicits._
import cats.effect._

import shapeless.record._

import io.circe._
import io.circe.generic.auto._
import io.circe.shapes._

import org.http4s.HttpRoutes
import org.http4s.EntityDecoder
import org.http4s.EntityEncoder
import org.http4s.dsl.io._
import org.http4s.circe._

object SuppliesController {

    object NameQueryParamMatcher extends QueryParamDecoderMatcher[String]("name")
    object IdQueryParamMatcher extends QueryParamDecoderMatcher[String]("id")

    implicit def jsonDecoder[A: Decoder]: EntityDecoder[IO, A] = CirceEntityDecoder.circeEntityDecoder
    implicit def jsonEncoder[A: Encoder]: EntityEncoder[IO, A] = CirceEntityEncoder.circeEntityEncoder

    def httpRoutes(implicit
        users: UserService[IO],
        supplies: SuppliesService[IO]
    ): HttpRoutes[IO] = HttpRoutes.of[IO] {
        
        case req @ GET -> Root / "api" / "supplies" => auth(users, req)(
            userId => handleInternalError(
                Ok(supplies.list(userId))
            )
        )

        case req @ GET -> Root / "api" / "supplies" / "single" :? IdQueryParamMatcher(id) => auth(users, req)(
            _ => supplies.retrieve(id).flatMap {
                case None => NotFound(s"FoodItem with ID $id not found")
                case Some(value) => Ok(value)
            }
        )

        case req @ GET -> Root / "api" / "supplies" / "calories" => auth(users, req)(
            user => handleInternalError(
                Ok(supplies.sumCalories(user))
            )
        )

        case req @ GET -> Root / "api" / "supplies" / "search" :? NameQueryParamMatcher(nameSearch) => auth(users, req)(
            userId => handleInternalError(
                supplies.findName(userId)(nameSearch)
                    .flatMap(list => Ok(list))
            )
        )

        case req @ DELETE -> Root / "api" / "supplies" :? IdQueryParamMatcher(id) => auth(users, req)(
            userId => handleInternalError(
                supplies.retrieve(id)
                    .flatMap {
                        case None => NotFound(s"FoodItem with ID $id not found")
                        case Some(value) =>
                            if (value('userId) == userId) supplies.delete(id) *> Ok()
                            else BadRequest(s"User '$userId' is not permitted to delete supplies with ID $id")
                    }
            )
        )

        case req @ POST -> Root / "api" / "supplies" => auth(users, req)(
            userId => handleInternalError(
                req.as[NewFoodItem]
                    .flatMap(posted => supplies.create(userId)(posted) *> Ok())
            )
        )

        case req @ POST -> Root / "api" / "supplies" / "update" => auth(users, req)(
            userId => handleInternalError(
                req.as[FoodItem]
                    .flatMap(posted => supplies.overwrite(userId)(posted) *> Ok())
            )
        )
    }
}