package emergencymanager.backend.controller

import emergencymanager.commons.data._
import emergencymanager.commons.validation.FoodItemValidation._
import emergencymanager.commons.validation.FoodItemValidation

import emergencymanager.backend.SuppliesService
import emergencymanager.backend.UserService

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

import java.util.Base64

object SuppliesController {

    object NameQueryParamMatcher extends QueryParamDecoderMatcher[String]("name")
    object IdQueryParamMatcher extends QueryParamDecoderMatcher[String]("id")

    implicit def jsonDecoder[A: Decoder]: EntityDecoder[IO, A] = CirceEntityDecoder.circeEntityDecoder
    implicit def jsonEncoder[A: Encoder]: EntityEncoder[IO, A] = CirceEntityEncoder.circeEntityEncoder

    val base64Decoder = Base64.getDecoder()

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
                case None => NotFound(s"Item with ID $id not found")
                case Some(value) => Ok(value)
            }
        )

        case req @ GET -> Root / "api" / "supplies" / "calories" => auth(users, req)(
            user => handleInternalError(
                Ok(supplies.sumCalories(user))
            )
        )

        case req @ POST -> Root / "api" / "supplies" / "search" => auth(users, req)(
            userId => handleInternalError(
                req.as[NameSearch]
                    .flatMap(nameSearch => supplies.findName(userId)(nameSearch("name")))
                    .flatMap(list => Ok(list))
            )
        )

        case req @ DELETE -> Root / "api" / "supplies" :? IdQueryParamMatcher(id) => auth(users, req)(
            userId => handleInternalError(
                supplies.retrieve(id)
                    .flatMap {
                        case None => NotFound(s"Item with ID $id not found")
                        case Some(value) =>
                            if (value("userId") == userId) supplies.delete(id) *> Ok()
                            else BadRequest(s"User '$userId' is not permitted to delete item with ID $id")
                    }
            )
        )

        case req @ POST -> Root / "api" / "supplies" => auth(users, req)(
            userId => handleInternalError(
                req.as[FoodItem.NewItem]
                    .flatMap { foodItem =>

                        val validated = FoodItemValidation.validate(foodItem)

                        val validationErrors = validated
                            .toList
                            .flatMap(_.toEither.left.toOption)
                            .flatMap(_.toList)
                            
                        if (validationErrors.isEmpty) {
                            supplies.create(userId)(foodItem) *> Ok()
                        } else {
                            BadRequest(makeValidationErrorMessage(validationErrors))
                        }
                    }
            )
        )

        case req @ POST -> Root / "api" / "supplies" / "update" => auth(users, req)(
            userId => handleInternalError(
                req.as[FoodItem.IdItem]
                    .flatMap { foodItem =>

                        val validated = FoodItemValidation.validate(foodItem)

                        val validationErrors = validated
                            .toList
                            .flatMap(_.toEither.left.toOption)
                            .flatMap(_.toList)
                            
                        if (validationErrors.isEmpty) {
                            supplies.overwrite(userId)(foodItem) *> Ok()
                        } else {
                            BadRequest(makeValidationErrorMessage(validationErrors))
                        }
                    }
            )
        )
    }

    private def makeValidationErrorMessage(validationErrors: List[FoodItemInvalid]) =
        "Validation errors occured: " + validationErrors.map(_.message).reduceLeft((e1, e2) => e1 + "; " + e2)
}