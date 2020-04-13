package emergencymanager.backend

import emergencymanager.commons.data._

import emergencymanager.backend.data._

import emergencymanager.backend.dynamodb._
import emergencymanager.backend.dynamodb.implicits._

import emergencymanager.backend.database._
import emergencymanager.backend.database.implicits._

import emergencymanager.backend.controller._

import cats.effect._
import cats.implicits._

import org.http4s.implicits._
import org.http4s.server.blaze._

import software.amazon.awssdk.regions.Region
import org.mongodb.scala.MongoClient
import emergencymanager.backend.database.Collection

object Application extends IOApp {

  implicit val region = Region.EU_CENTRAL_1

  implicit val mongoDatabase = MongoClient
    .apply("mongodb://backend:0daj8dwjna8dw@ec2-3-125-2-232.eu-central-1.compute.amazonaws.com:27017/?authSource=em&readPreference=primary&ssl=false")
    .getDatabase("em")


  implicit val suppliesCollection = Collection[FoodItem.UserItem2]("fooditems")

  implicit val emSuppliesDynamoDb = DynamoDb.io[FoodItem.SearchableUserItem]("EMSupplies")
  implicit val userDynamoDb = DynamoDb.io[User]("EMUser")
  implicit val tokenDb = DynamoDb.io[Token]("EMToken")

  val httpRoutes = (
    SuppliesController.httpRoutes <+>
    UserController.httpRoutes <+>
    FrontendController.httpRoutes
  ).orNotFound
  
  def run(args: List[String]): IO[ExitCode] = args match {
    case "transfer" :: Nil => DatabaseUtil
      .transfer
      .as(ExitCode.Success)
    case "list" :: Nil => suppliesCollection.list
      .map(list => println(list))
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
