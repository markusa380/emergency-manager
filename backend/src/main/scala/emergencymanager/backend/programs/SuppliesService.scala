package emergencymanager.backend.programs.service

import emergencymanager.backend.programs.DynamoDb
import emergencymanager.backend.algebra.serde.dynamodb.ToAttributeValue

import emergencymanager.commons.data.FoodItem

import cats.effect.IO

import shapeless._
import record._

trait SuppliesService[F[_]] {
    def createOrOverwrite(item: FoodItem.WithUserId): F[Unit]
    def delete(id: String): F[Unit]
    def retrieve(id: String): F[Option[FoodItem.WithUserId]]
    def findName(name: String, user: String): F[List[FoodItem.WithUserId]]
    def list(user: String): F[List[FoodItem.WithUserId]]
    def sumCalories(user: String): F[Double]
}

object SuppliesService {
    
    def apply[F[_]](implicit s: SuppliesService[F]): SuppliesService[F] = s

    implicit def suppliesServiceIo(implicit
        dynamoDb: DynamoDb[IO, FoodItem.WithUserId]
    ): SuppliesService[IO] = new SuppliesService[IO] {

        def createOrOverwrite(item: FoodItem.WithUserId): IO[Unit] = dynamoDb.save(item)

        def delete(id: String): IO[Unit] = dynamoDb.delete(id)

        def retrieve(id: String): IO[Option[FoodItem.WithUserId]] = {
            dynamoDb.loadOption(id)
        }

        def findName(name: String, user: String): IO[List[FoodItem.WithUserId]] = {
                dynamoDb.filter(
                expression = s"contains (name, :namevalue) and contains (user, :uservalue)",
                expressionAttributeValues = Map(
                    ":namevalue" -> ToAttributeValue.to(name),
                    ":uservalue" -> ToAttributeValue.to(user)
                )
            )
        }

        def list(user: String): IO[List[FoodItem.WithUserId]] = {
            dynamoDb.filter(
                expression = s"contains (userId, :useridvalue)",
                expressionAttributeValues = Map(
                    ":useridvalue" -> ToAttributeValue.to(user)
                )
            )
        }

        def sumCalories(user: String): IO[Double] = {
            list(user)
                .map(list =>
                    list.foldLeft(0.0){ (sum, sup) =>
                        val caloriesPerGram = sup.get(Symbol("kiloCalories")) / 100.0   
                        sum + sup.get(Symbol("weightGrams")) * caloriesPerGram * sup.get(Symbol("number"))
                    }
                )
        }
    }
}