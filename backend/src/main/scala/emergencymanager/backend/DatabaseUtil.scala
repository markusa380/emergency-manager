package emergencymanager.backend

import emergencymanager.commons.data._
import emergencymanager.commons.implicits._

import emergencymanager.backend.dynamodb._
import emergencymanager.backend.dynamodb.implicits._

import cats.effect._
import cats.implicits._

import scala.concurrent.duration._

import emergencymanager.backend.database.Collection
import emergencymanager.backend.database.implicits._

import software.amazon.awssdk.regions.Region
import org.mongodb.scala.MongoDatabase

object DatabaseUtil {

    def transfer(implicit
        database: MongoDatabase,
        region: Region,
        timer: Timer[IO],
        ce: ConcurrentEffect[IO]
    ): IO[Unit] = {
    
        val suppliesCollection = Collection[FoodItem.UserItem]("fooditems")
        val legacyEmSuppliesDynamoDb = DynamoDb.io[FoodItem.OldSearchableUserItem]("EMSupplies")

        legacyEmSuppliesDynamoDb.list
            .flatMap( _
                .foldLeft(IO.unit) { (io, legacyItem) => 
                    io *> IO.sleep(200.millis) *> IO(println(s"Saving item $legacyItem")) *> suppliesCollection.save(
                        legacyItem.withoutSearchName.toUserItemV2
                    )
                }
            )
    }
}