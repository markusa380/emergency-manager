package emergencymanager.backend

import emergencymanager.commons.data._
import emergencymanager.commons.implicits._

import emergencymanager.backend.dynamodb._
import emergencymanager.backend.dynamodb.implicits._

import cats.effect._
import cats.implicits._

import shapeless.record._

import scala.concurrent.duration._

import software.amazon.awssdk.regions.Region

object DatabaseUtil {

    def schemaUpdate1(implicit
        suppliesDb: DynamoDb[IO, FoodItem.SearchableUserItem],
        region: Region,
        timer: Timer[IO]
    ): IO[Unit] = {
    
        val legacyEmSuppliesDynamoDb = DynamoDb.io[FoodItem.UserItem]("EMSupplies")

        legacyEmSuppliesDynamoDb.list
            .flatMap( _
                .foldLeft(IO.unit) { (io, legacyItem) => 
                    io *> IO.sleep(200.millis) *> suppliesDb.save(
                        legacyItem.withSearchName(
                            legacyItem("name").toLowerCase
                        )
                    )
                }
            )
    }
}