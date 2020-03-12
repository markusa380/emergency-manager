package emergencymanager.backend.programs.controller

import emergencymanager.commons.data.Supplies

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
import cats.data.NonEmptyList
import org.http4s.Challenge

object SuppliesController {

    object NameQueryParamMatcher extends QueryParamDecoderMatcher[String]("name")

    implicit def jsonDecoder[A: Decoder]: EntityDecoder[IO, A] = CirceEntityDecoder.circeEntityDecoder
    implicit def jsonEncoder[A: Encoder]: EntityEncoder[IO, A] = CirceEntityEncoder.circeEntityEncoder

    def httpRoutes(supplies: SuppliesService, users: UserService): HttpRoutes[IO] = HttpRoutes.of[IO] {
        case req @ GET -> Root / "supplies" => authenticate(users)(
            req,
            user => Ok(supplies.list)
        )
        case req @ GET -> Root / "supplies" / "calories" => authenticate(users)(
            req,
            user => Ok(supplies.sumCalories)
        )
        case req @ GET -> Root / "supplies" / "search" :? NameQueryParamMatcher(nameSearch) => authenticate(users)(
            req,
            user => Ok(supplies.findName(nameSearch))
        )
        case req @ POST -> Root / "supplies" => authenticate(users)(
            req,
            user => Ok(
                req.as[Supplies]
                    .flatMap(supplies.createOrOverwrite)
            )
        )
    }

    def authenticate(users: UserService)(req: Request[IO], f: String => IO[Response[IO]]): IO[Response[IO]] = {
        req.cookies.find(_.name.equals("token")) match {
            case Some(cookie) => users.challenge(cookie.content) flatMap {
                case Some(userId) => f(userId)
                case None => BadRequest("Auth header is invalid")
            }
            case None => NetworkAuthenticationRequired()
        }
    }
}