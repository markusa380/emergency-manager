package emergencymanager.backend.programs

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

package object controller {
  
    def extractToken(req: Request[IO]): Option[String] = req
        .cookies
        .find(_.name.equals("token"))
        .map(_.content)

    def authenticate(users: UserService)(req: Request[IO], f: String => IO[Response[IO]]): IO[Response[IO]] = {
        extractToken(req) match {
            case Some(cookie) => users.challenge(cookie) flatMap {
                case Some(userId) => f(userId)
                case None => BadRequest("Authentication cookie is invalid.")
            }
            case None => NetworkAuthenticationRequired()
        }
    }
}
