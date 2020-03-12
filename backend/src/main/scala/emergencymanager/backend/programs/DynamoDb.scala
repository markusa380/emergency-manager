package backend.programs

import cats.implicits._

import cats.effect._

import shapeless._

import backend.algebra.serde.dynamodb._

import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.{DynamoDbClient => JavaDynamoDbClient}
import software.amazon.awssdk.services.dynamodb.model._

import scala.jdk.CollectionConverters._

case class DynamoDb[D](
    region: Region,
    table: String
)(implicit 
    fromDynamoDbItem: FromDynamoDbItem[D],
    toDynamoDbItem: ToDynamoDbItem[D]
) {

    val ddb = JavaDynamoDbClient.builder()
        .region(region)
        .build()

    def load(id: String): IO[D] = for {
        result <- IO(
            ddb.getItem(
                GetItemRequest
                    .builder
                    .tableName(table)
                    .key(Map("id" -> AttributeValue.builder().s(id).build).asJava)
                    .build
            )
        )
        item = Option(result.item)
            .toRight(new IllegalStateException(s"Could not find item with id $id"))
            .map(_.asScala.toMap)
        parsed <- IO.fromEither(item.flatMap(fromDynamoDbItem.apply))
    } yield parsed

    def save(d: D): IO[Unit] = for {
        _ <- IO(
            ddb.putItem(
                PutItemRequest.builder
                    .item(toDynamoDbItem(d).asJava)
                    .tableName(table)
                    .build
            )
        )
    } yield ()

    private def parseItemList(items: java.util.Collection[java.util.Map[String, AttributeValue]]) = items
        .asScala
        .toList
        .map(_.asScala.toMap)
        .traverse(fromDynamoDbItem.apply)

    def list: IO[List[D]] = for {
        response <- IO(
            ddb.scan(
                ScanRequest.builder()
                    .tableName(table)
                    .build()
            )
        )
        items = parseItemList(response.items)
        parsed <- IO.fromEither(items)
    } yield parsed

    def filter(
        expression: String,
        expressionAttributeValues: Map[String, AttributeValue] = Map.empty,
        expressionAttributeNames: Map[String, String] = Map.empty
    ): IO[List[D]] = for {
        response <- IO(
            ddb.scan(
                ScanRequest.builder()
                    .filterExpression(expression)
                    .expressionAttributeValues(expressionAttributeValues.asJava)
                    .expressionAttributeNames(expressionAttributeNames.asJava)
                    .tableName(table)
                    .build()
            )
        )
        items = parseItemList(response.items)
        parsed <- IO.fromEither(items)
    } yield parsed

    def delete(id: String): IO[Unit] = IO(
        ddb.deleteItem(
            DeleteItemRequest.builder()
                .key(Map("id" -> AttributeValue.builder().s(id).build).asJava)
                .tableName(table)
                .build
        )
    ) *> IO.unit
}