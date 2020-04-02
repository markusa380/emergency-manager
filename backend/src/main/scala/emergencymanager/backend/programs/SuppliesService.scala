package emergencymanager.backend.programs.service

import emergencymanager.backend.programs.DynamoDb
import emergencymanager.backend.algebra.serde.dynamodb.ToAttributeValue

import emergencymanager.backend.data.EMSupplies
import cats.effect.IO

trait SuppliesService[F[_]] {
    def createOrOverwrite(s: EMSupplies): F[Unit]
    def delete(id: String): F[Unit]
    def retrieve(id: String): F[Option[EMSupplies]]
    def findName(name: String, user: String): F[List[EMSupplies]]
    def list(user: String): F[List[EMSupplies]]
    def sumCalories(user: String): F[Double]
}

object SuppliesService {
    
    def apply[F[_]](implicit s: SuppliesService[F]): SuppliesService[F] = s

    implicit def suppliesServiceIo(implicit dynamoDb: DynamoDb[IO, EMSupplies]): SuppliesService[IO] = new SuppliesService[IO] {
        
        def createOrOverwrite(s: EMSupplies): IO[Unit] = dynamoDb.save(s)

        def delete(id: String): IO[Unit] = dynamoDb.delete(id)

        def retrieve(id: String): IO[Option[EMSupplies]] = dynamoDb.loadOption(id)

        def findName(name: String, user: String): IO[List[EMSupplies]] = dynamoDb.filter(
            expression = s"contains (name, :namevalue) and contains (user, :uservalue)",
            expressionAttributeValues = Map(
                ":namevalue" -> ToAttributeValue.to(name),
                ":uservalue" -> ToAttributeValue.to(user)
            )
        )

        def list(user: String): IO[List[EMSupplies]] = dynamoDb.filter(
            expression = s"contains (userId, :useridvalue)",
            expressionAttributeValues = Map(
                ":useridvalue" -> ToAttributeValue.to(user)
            )
        )

        def sumCalories(user: String): IO[Double] = list(user)
            .map(list =>
                list.foldLeft(0.0){ (sum, sup) =>
                    val caloriesPerGram = sup.kiloCalories / 100.0   
                    sum + sup.weightGrams * caloriesPerGram * sup.number
                }
            )
    }
}