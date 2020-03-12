package emergencymanager.backend.programs.controller

import emergencymanager.commons.data.Supplies
import emergencymanager.backend.programs.service.UserService
import emergencymanager.commons.data.Auth

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
import org.http4s.Header
import org.http4s.Headers

object UserController {

    implicit def jsonDecoder[A: Decoder]: EntityDecoder[IO, A] = CirceEntityDecoder.circeEntityDecoder
    implicit def jsonEncoder[A: Encoder]: EntityEncoder[IO, A] = CirceEntityEncoder.circeEntityEncoder

    def httpRoutes(users: UserService): HttpRoutes[IO] = HttpRoutes.of[IO] {
        case req @ POST -> Root / "login" =>
            req.as[Auth]
                .flatMap(auth =>
                    users.login(auth.username, auth.password)
                        .flatMap {
                            case Some(token) => Ok(
                                Header(
                                    "Set-Cookie",
                                    s"token=$token; Max-Age=${users.maxTokenAgeSeconds}"
                                )
                            )
                            case None => BadRequest("Username or password incorrect.")
                        }
                )
                .recoverWith(t => InternalServerError(t.getMessage()))
    }
}