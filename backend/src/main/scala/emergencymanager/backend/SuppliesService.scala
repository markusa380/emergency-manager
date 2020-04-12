package emergencymanager.backend

import emergencymanager.backend.dynamodb._

import emergencymanager.commons.data._
import emergencymanager.commons.implicits._

import cats.effect.IO
import cats.implicits._

import shapeless.record._

import java.{util => ju}

trait SuppliesService[F[_]] {
    def create(userId: String)(item: FoodItem.NewItem): F[Unit]
    def overwrite(userId: String)(item: FoodItem.IdItem): F[Unit]
    def delete(id: String): F[Unit]
    def retrieve(id: String): F[Option[FoodItem.UserItem]]
    def findName(user: String)(name: String): F[List[FoodItem.IdItem]]
    def list(userId: String): F[List[FoodItem.IdItem]]
    def sumCalories(userId: String): F[Double]
}

object SuppliesService {
    
    def apply[F[_]](implicit s: SuppliesService[F]): SuppliesService[F] = s

    implicit def suppliesServiceIo(implicit
        dynamoDb: DynamoDb[IO, FoodItem.UserItem]
    ): SuppliesService[IO] = new SuppliesService[IO] {

        def create(userId: String)(item: FoodItem.NewItem): IO[Unit] = dynamoDb
            .save(
                item.withId(generateId).withUserId(userId)
            )

        private def generateId = ju.UUID.randomUUID.toString

        def overwrite(userId: String)(item: FoodItem.IdItem): IO[Unit] = dynamoDb
            .save(
                item.withUserId(userId)
            )

        def delete(id: String): IO[Unit] = dynamoDb.delete(id)

        def retrieve(id: String): IO[Option[FoodItem.UserItem]] = dynamoDb.loadOption(id)

        def findName(user: String)(name: String): IO[List[FoodItem.IdItem]] =
            // For cost saving we just list if the name is empty
            if (name.isEmpty) list(user)
            else dynamoDb
                .filter(
                    Query[FoodItem.UserItem].contains["name"](name)
                        and Query[FoodItem.UserItem].contains["userId"](user)
                )
                .nested
                .map(_.withoutUserId)
                .value

        def list(user: String): IO[List[FoodItem.IdItem]] = dynamoDb
            .filter(
                Query[FoodItem.UserItem].contains["userId"](user)
            )
            .nested
            .map(_.withoutUserId)
            .value

        def sumCalories(user: String): IO[Double] = list(user)
            .map(list =>
                list.foldLeft(0.0){ (sum, sup) =>
                    val caloriesPerGram = sup("kiloCalories") / 100.0   
                    sum + sup("weightGrams") * caloriesPerGram * sup("number")
                }
            )
    }
}