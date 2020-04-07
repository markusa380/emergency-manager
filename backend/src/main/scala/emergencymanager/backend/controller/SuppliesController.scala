package emergencymanager.backend.programs.controller

import emergencymanager.commons.data.FoodItem

import emergencymanager.backend.programs.service.SuppliesService
import emergencymanager.backend.programs.service.UserService

import cats.implicits._
import cats.effect._

import io.circe._
import io.circe.generic.auto._

import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.circe._
import org.http4s.EntityDecoder
import org.http4s.EntityEncoder

import shapeless.record._

import java.{util => ju}

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
            user => handleInternalError(
                Ok(supplies.list(user).map(_.map(FoodItem.toFoodItem)))
            )
        )

        case req @ GET -> Root / "api" / "supplies" / "single" :? IdQueryParamMatcher(id) => auth(users, req)(
            _ => supplies.retrieve(id).flatMap {
                case None => NotFound(s"FoodItem with ID $id not found")
                case Some(value) => Ok(FoodItem.toFoodItem(value))
            }
        )

        case req @ GET -> Root / "api" / "supplies" / "calories" => auth(users, req)(
            user => handleInternalError(
                Ok(supplies.sumCalories(user))
            )
        )

        case req @ GET -> Root / "api" / "supplies" / "search" :? NameQueryParamMatcher(nameSearch) => auth(users, req)(
            user => handleInternalError(
                supplies.findName(nameSearch, user)
                    .map(_.map(FoodItem.toFoodItem))
                    .flatMap(list => Ok(list))
            )
        )

        case req @ DELETE -> Root / "api" / "supplies" :? IdQueryParamMatcher(id) => auth(users, req)(
            user => handleInternalError(
                supplies.retrieve(id)
                    .flatMap {
                        case None => NotFound(s"FoodItem with ID $id not found")
                        case Some(value) =>
                            if(value.get(Symbol("userId")) == user) supplies.delete(id) *> Ok()
                            else BadRequest(s"User '$user' is not permitted to delete supplies with ID $id")
                    }
            )
        )

        case req @ POST -> Root / "api" / "supplies" => auth(users, req)(
            user => handleInternalError(
                req.as[FoodItem]
                    .flatMap(posted => supplies.retrieve(posted.id)
                        .map(retrieved => (posted, retrieved))
                    )
                    .flatMap {
                        case (posted, None) => saveSupplies(
                            user,
                            posted.copy(id = randomId)
                        ) *> Ok()
                        case (posted, Some(retrieved)) =>
                            if(retrieved.get(Symbol("userId")) == user) {
                                saveSupplies(user, posted) *> Ok()
                            } else BadRequest(s"User '$user' is not permitted to overwrite supplies with ID ${posted.id}")
                    }
                    
            )
        )
    }

    private def saveSupplies(user: String, s: FoodItem)(implicit supplies: SuppliesService[IO]): IO[Unit] =
        supplies.createOrOverwrite(s.withUserId(user))

    private def randomId = ju.UUID.randomUUID.toString
}