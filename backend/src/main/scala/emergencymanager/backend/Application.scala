package emergencymanager.backend

import emergencymanager.commons.data._

import emergencymanager.backend.data._

import emergencymanager.backend.dynamodb._
import emergencymanager.backend.dynamodb.implicits._

import emergencymanager.backend.controller._

import cats.effect._
import cats.implicits._

import org.http4s.implicits._
import org.http4s.server.blaze._

import software.amazon.awssdk.regions.Region

object Application extends IOApp {

  implicit val region = Region.EU_CENTRAL_1

  implicit val emSuppliesDynamoDb = DynamoDb.io[FoodItem.SearchableUserItem]("EMSupplies")
  implicit val userDynamoDb = DynamoDb.io[User]("EMUser")
  implicit val tokenDb = DynamoDb.io[Token]("EMToken")

  val httpRoutes = (
    SuppliesController.httpRoutes <+>
    UserController.httpRoutes <+>
    FrontendController.httpRoutes
  ).orNotFound
  
  def run(args: List[String]): IO[ExitCode] = args match {
    case "schemaUpdate1" :: Nil => DatabaseUtil
      .schemaUpdate1
      .as(ExitCode.Success)
    case _ => BlazeServerBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(httpRoutes)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }
}
