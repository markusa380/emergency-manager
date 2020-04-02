package emergencymanager.backend

import emergencymanager.backend.programs.service._
import emergencymanager.backend.programs.controller._

import cats.effect._
import cats.implicits._

import org.http4s.implicits._
import org.http4s.server.blaze._

import software.amazon.awssdk.regions.Region

object Application extends IOApp {

  implicit val region = Region.EU_CENTRAL_1

  val suppliesStorage = new SuppliesService
  val userService = new UserService

  val httpRoutes = (
    SuppliesController.httpRoutes(suppliesStorage, userService) <+>
    UserController.httpRoutes(userService) <+>
    FrontendController.httpRoutes
  ).orNotFound
  
  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(httpRoutes)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
