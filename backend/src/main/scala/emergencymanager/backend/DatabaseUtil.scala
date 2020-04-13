package emergencymanager.backend

import emergencymanager.commons.data._
import emergencymanager.commons.implicits._

import emergencymanager.backend.dynamodb._
import emergencymanager.backend.dynamodb.implicits._

import cats.effect._
import cats.implicits._

import scala.concurrent.duration._

import software.amazon.awssdk.regions.Region
import emergencymanager.backend.database.Collection

object DatabaseUtil {

    def transfer(implicit
        suppliesDb: Collection[IO, FoodItem.UserItem2],
        region: Region,
        timer: Timer[IO]
    ): IO[Unit] = {
    
        val legacyEmSuppliesDynamoDb = DynamoDb.io[FoodItem.SearchableUserItem]("EMSupplies")

        legacyEmSuppliesDynamoDb.list
            .flatMap( _
                .foldLeft(IO.unit) { (io, legacyItem) => 
                    io *> IO.sleep(200.millis) *> IO(println(s"Saving item $legacyItem")) *> suppliesDb.save(
                        legacyItem.withoutSearchName.toUserItemV2
                    )
                }
            )
    }
}