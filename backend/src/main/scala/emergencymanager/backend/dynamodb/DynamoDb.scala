package emergencymanager.backend.dynamodb

import cats.implicits._

import cats.effect._

import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.{DynamoDbClient => JavaDynamoDbClient}
import software.amazon.awssdk.services.dynamodb.model._

import scala.jdk.CollectionConverters._

trait DynamoDb[F[_], D] {
    def loadOption(id: String): F[Option[D]]
    def load(id: String): F[D]
    def save(document: D): F[Unit]
    def list: F[List[D]]
    def filter(expression: String, expressionAttributeValues: Map[String, AttributeValue]): F[List[D]]
    def delete(id: String): F[Unit]
}

object DynamoDb {

    def io[D](
        table: String
    )(implicit
        fromDynamoDbItem: FromDynamoDbItem[D],
        toDynamoDbItem: ToDynamoDbItem[D],
        region: Region
    ): DynamoDb[IO, D] = new DynamoDb[IO, D] {

        val ddb = JavaDynamoDbClient.builder()
            .region(region)
            .build()

        def loadOption(id: String): IO[Option[D]] = for {
            result <- IO(
                ddb.getItem(
                    GetItemRequest
                        .builder
                        .tableName(table)
                        .key(Map("id" -> AttributeValue.builder().s(id).build).asJava)
                        .build
                )
            )
            item <- Option(result.item)
                .filter(!_.isEmpty)
                .map(_.asScala.toMap) match {
                    case Some(value) => IO.fromEither(fromDynamoDbItem.apply(value)).map(Some.apply)
                    case None => IO(None)
                }
        } yield item

        def load(id: String): IO[D] = loadOption(id)
            .map(_.toRight(new IllegalStateException(s"Could not find item with ID $id")))
            .flatMap(IO.fromEither)

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
            expressionAttributeValues: Map[String, AttributeValue]
            // expressionAttributeNames: Map[String, String] = Map.empty
        ): IO[List[D]] = for {
            response <- IO(
                ddb.scan(
                    ScanRequest.builder()
                        .filterExpression(expression)
                        .expressionAttributeValues(expressionAttributeValues.asJava)
                        // .expressionAttributeNames(expressionAttributeNames)
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
                    .key(Map("id" -> AttributeValue.builder().s(id).build).asJava) // TODO
                    .tableName(table)
                    .build
            )
        ) *> IO.unit

    }
}