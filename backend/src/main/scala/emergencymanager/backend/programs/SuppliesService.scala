package emergencymanager.backend.programs.service

import emergencymanager.backend.programs.DynamoDb
import emergencymanager.backend.algebra.serde.dynamodb.ToAttributeValue

import emergencymanager.commons.data._
import emergencymanager.commons.ops._

import cats.effect.IO
import cats.implicits._

import shapeless.record._

import java.{util => ju}

trait SuppliesService[F[_]] {
    def create(userId: String)(item: NewFoodItem): F[Unit]
    def overwrite(userId: String)(item: FoodItem): F[Unit]
    
    def delete(id: String): F[Unit]
    def retrieve(id: String): F[Option[UserFoodItem]]
    def findName(user: String)(name: String): F[List[FoodItem]]
    def list(userId: String): F[List[FoodItem]]

    def sumCalories(userId: String): F[Double]
}

object SuppliesService {
    
    def apply[F[_]](implicit s: SuppliesService[F]): SuppliesService[F] = s

    implicit def suppliesServiceIo(implicit
        dynamoDb: DynamoDb[IO, UserFoodItem]
    ): SuppliesService[IO] = new SuppliesService[IO] {

        def create(userId: String)(item: NewFoodItem): IO[Unit] = dynamoDb
            .save(
                item.withId(generateId).withUserId(userId)
            )

        private def generateId = ju.UUID.randomUUID.toString

        def overwrite(userId: String)(item: FoodItem): IO[Unit] = dynamoDb
            .save(
                item.withUserId(userId)
            )

        def delete(id: String): IO[Unit] = dynamoDb.delete(id)

        def retrieve(id: String): IO[Option[UserFoodItem]] = dynamoDb.loadOption(id)

        def findName(user: String)(name: String): IO[List[FoodItem]] = dynamoDb
            .filter(
                expression = s"contains (name, :namevalue) and contains (user, :uservalue)",
                expressionAttributeValues = Map(
                    ":namevalue" -> ToAttributeValue.to(name),
                    ":uservalue" -> ToAttributeValue.to(user)
                )
            )
            .nested
            .map(_.withoutUserId)
            .value

        def list(user: String): IO[List[FoodItem]] = dynamoDb
            .filter(
                expression = s"contains (userId, :useridvalue)",
                expressionAttributeValues = Map(
                    ":useridvalue" -> ToAttributeValue.to(user)
                )
            )
            .nested
            .map(_.withoutUserId)
            .value

        def sumCalories(user: String): IO[Double] = list(user)
            .map(list =>
                list.foldLeft(0.0){ (sum, sup) =>
                    val caloriesPerGram = sup('kiloCalories) / 100.0   
                    sum + sup('weightGrams) * caloriesPerGram * sup('number)
                }
            )
    }
}