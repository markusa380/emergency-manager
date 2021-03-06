package emergencymanager.backend

import emergencymanager.backend.database._
import emergencymanager.backend.database.implicits._

import emergencymanager.commons.data._

import cats.effect._
import cats.implicits._

import shapeless.record._
import shapeless.labelled._

import org.mongodb.scala.MongoDatabase

trait SuppliesService[F[_]] {
    def create(userId: String)(item: FoodItem.NewItem): F[Unit]
    def overwrite(userId: String)(item: FoodItem.IdItem): F[Unit]
    def delete(userId: String)(id: String): F[Unit]
    def retrieve(userId: String)(id: String): F[Option[FoodItem.IdItem]]
    def findName(userId: String)(name: String): F[List[FoodItem.IdItem]]
    def list(userId: String): F[List[FoodItem.IdItem]]
    def sumCalories(userId: String): F[Double]
}

object SuppliesService {
    
    def apply[F[_]](implicit s: SuppliesService[F]): SuppliesService[F] = s

    implicit def suppliesServiceIo(implicit
        database: MongoDatabase,
        ce: ConcurrentEffect[IO],
        ctx: ContextShift[IO]
    ): SuppliesService[IO] = new SuppliesService[IO] {

        val collection = Collection[FoodItem.UserItem]("fooditems")

        def create(userId: String)(item: FoodItem.NewItem): IO[Unit] = {
            val userItem = item + field["userId"](userId)
            collection.save(userItem)
        }
        
        def overwrite(userId: String)(item: FoodItem.IdItem): IO[Unit] = {
            val userItem = item + field["userId"](userId)
            val aligned = userItem.align[collection.WithId]
            collection.overwrite(aligned)
        }
        
        def delete(userId: String)(id: String): IO[Unit] = {
            val query = Query[collection.WithId].idEquals(id)
                .and(Query[collection.WithId].equals["userId"](userId))

            collection.deleteOne(query)
        }
        
        def retrieve(userId: String)(id: String): IO[Option[FoodItem.IdItem]] = {
            val query = Query[collection.WithId].idEquals(id)
                .and(Query[collection.WithId].equals["userId"](userId))

            collection.findOption(query)
                .nested
                .map(userItem => (userItem - "userId").align[FoodItem.IdItem])
                .value
        }
        
        def findName(userId: String)(name: String): IO[List[FoodItem.IdItem]] =
            if (name.trim.isEmpty) list(userId)
            else {
                val query = Query[collection.WithId].equals["userId"](userId)
                    .and(Query[collection.WithId].search["name"](name))

                collection
                    .find(query)
                    .nested
                    .map(userItem => (userItem - "userId").align[FoodItem.IdItem])
                    .value
            }
        
        def list(userId: String): IO[List[FoodItem.IdItem]] = collection
            .find(Query[collection.WithId].equals["userId"](userId))
            .nested
            .map(userItem => (userItem - "userId").align[FoodItem.IdItem])
            .value
        
        def sumCalories(userId: String): IO[Double] = list(userId)
            .map(list =>
                list.foldLeft(0.0){ (sum, sup) =>
                    val caloriesPerGram = sup("kiloCalories") / 100.0   
                    sum + sup("weightGrams") * caloriesPerGram * sup("number")
                }
            )
    }
}