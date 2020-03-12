package emergencymanager.backend.programs

import software.amazon.awssdk.regions.Region
import emergencymanager.commons.data.Supplies
import cats.effect.IO
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import emergencymanager.backend.algebra.serde.dynamodb.ToAttributeValue

case class SuppliesStorage(
    region: Region
) {
    val dynamoDb = DynamoDb[Supplies](region, "Supplies")

    def createOrOverwrite(s: Supplies): IO[Unit] = dynamoDb.save(s)

    def delete(id: String): IO[Unit] = dynamoDb.delete(id)

    def addNumber(id: String, number: Int): IO[Unit] = dynamoDb.load(id)
        .map(old => old.copy(number = old.number + number))
        .flatMap(dynamoDb.save)

    def findName(name: String): IO[List[Supplies]] = dynamoDb.filter(
        expression = s"contains (name, :str)",
        expressionAttributeValues = Map(":str" -> ToAttributeValue.to(name))
    )

    def list: IO[List[Supplies]] = dynamoDb.list

    def sumCalories: IO[Double] = dynamoDb.list
        .map(list =>
            list.foldLeft(0.0){ (sum, sup) =>
                val caloriesPerGram = sup.kiloCalories / 100.0   
                sup.weightGrams * caloriesPerGram * sup.number
            }
        )
}