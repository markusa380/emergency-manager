package emergencymanager.backend.programs.controller

import emergencymanager.backend.programs.service.UserService
import emergencymanager.commons.data.Auth

import cats.implicits._
import cats.effect._

import io.circe._
import io.circe.generic.auto._

import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.circe._
import org.http4s.EntityDecoder
import org.http4s.Header

object UserController {

    implicit def jsonDecoder[A: Decoder]: EntityDecoder[IO, A] = CirceEntityDecoder.circeEntityDecoder

    def httpRoutes(implicit users: UserService[IO]): HttpRoutes[IO] = HttpRoutes.of[IO] {
        case req @ POST -> Root / "api" / "login" =>
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

        case req @ GET -> Root / "api" / "challenge" => extractToken(req) match {
            case Some(token) => Ok(
                users.challenge(token).map {
                    case Some(_) => "true"
                    case None => "false"
                }
            )
            case None => Ok("false")
        }
            
    }
}