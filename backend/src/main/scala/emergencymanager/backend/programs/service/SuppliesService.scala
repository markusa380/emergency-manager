package emergencymanager.backend.programs.service

import emergencymanager.backend.programs.DynamoDb
import emergencymanager.backend.algebra.serde.dynamodb.ToAttributeValue

import software.amazon.awssdk.regions.Region
import emergencymanager.backend.data.EMSupplies
import cats.effect.IO
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class SuppliesService(
    implicit region: Region
) {
    val dynamoDb = DynamoDb[EMSupplies]("EMSupplies")

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