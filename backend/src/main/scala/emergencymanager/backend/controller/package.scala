package emergencymanager.backend

import emergencymanager.backend.UserService

import cats.effect._

import org.http4s.dsl.io._
import org.http4s.Request
import org.http4s.Response

import scala.util._


package object controller {
  
    def extractToken(req: Request[IO]): Option[String] = req
        .cookies
        .find(_.name.equals("token"))
        .map(_.content)

    def authenticate(users: UserService[IO])(req: Request[IO], f: String => IO[Response[IO]]): IO[Response[IO]] = {
        extractToken(req) match {
            case Some(cookie) => users.challenge(cookie) flatMap {
                case Some(userId) => f(userId)
                case None => BadRequest("Authentication cookie is invalid.")
            }
            case None => NetworkAuthenticationRequired()
        }
    }

    def auth(users: UserService[IO], req: Request[IO])(f: String => IO[Response[IO]]): IO[Response[IO]] = 
        authenticate(users)(req, f)

    def handleInternalError(io: => IO[Response[IO]]) = Try(
        io
            .attempt
            .flatMap {
                case Left(value) => InternalServerError(value.getMessage)
                case Right(value) => IO(value)
            }
    ) match {
        case Failure(exception) => exception.printStackTrace; println(exception.getMessage()); InternalServerError(exception.getMessage())
        case Success(value) => value
    }
}
