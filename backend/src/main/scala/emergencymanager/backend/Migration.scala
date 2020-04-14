package emergencymanager.backend

import emergencymanager.backend.ApplicationUtil._
import emergencymanager.backend.controller._

import cats.effect._
import cats.implicits._

import org.http4s.implicits._
import org.http4s.server.blaze._

import software.amazon.awssdk.regions.Region

import emergencymanager.backend.dynamodb._
import emergencymanager.backend.dynamodb.implicits._

import emergencymanager.backend.database._
import emergencymanager.backend.database.implicits._

import emergencymanager.commons.data._
import emergencymanager.commons.implicits._

import scala.concurrent.duration._

object Migration extends IOApp {
  
  def run(args: List[String]): IO[ExitCode] =
    loadApplicationConf.flatMap(conf =>
      createMongoDatabase(conf).flatMap { mongoDb =>
        implicit val m = mongoDb

        implicit val region = Region.EU_CENTRAL_1
        val legacySupplies = DynamoDb.io[FoodItem.OldUserItem]("EMSupplies")

        val newSupplies = Collection[FoodItem.UserItem]("fooditems")

        legacySupplies.list
            .flatMap(
                _.map(i => i.toUserItemV2)
                    .map(newSupplies.save)
                    .reduce(_ *> IO.sleep(100.millis) *> _)
            )
            .as(ExitCode.Success)
      }
    )
}