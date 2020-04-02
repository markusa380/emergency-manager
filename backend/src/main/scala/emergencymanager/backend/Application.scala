package emergencymanager.backend

import emergencymanager.backend.programs._
import emergencymanager.backend.programs.controller._

import cats.effect._
import cats.implicits._

import org.http4s.implicits._
import org.http4s.server.blaze._

import software.amazon.awssdk.regions.Region
import emergencymanager.backend.programs.DynamoDb
import emergencymanager.backend.data.EMSupplies
import emergencymanager.backend.data.User
import emergencymanager.backend.data.Token

object Application extends IOApp {

  implicit val region = Region.EU_CENTRAL_1
  implicit val emSuppliesDynamoDb = DynamoDb.io[EMSupplies]("EMSupplies")
  implicit val userDynamoDb = DynamoDb.io[User]("EMUser")
  implicit val tokenDb = DynamoDb.io[Token]("EMToken")

  val httpRoutes = (
    SuppliesController.httpRoutes <+>
    UserController.httpRoutes <+>
    FrontendController.httpRoutes
  ).orNotFound
  
  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(80, "0.0.0.0")
      .withHttpApp(httpRoutes)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
