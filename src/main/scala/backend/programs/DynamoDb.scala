package backend.programs

import cats.effect._

import shapeless._

import backend.algebra.FromDynamoDbItem

import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.{DynamoDbClient => JavaDynamoDbClient}
import software.amazon.awssdk.services.dynamodb.model._

import scala.jdk.CollectionConverters._

trait DynamoDb[D] {
    def load(id: String): IO[D]
}

object DynamoDb {
    def apply[D, R <: HList](
        region: Region,
        table: String
    )(implicit
        labelledGeneric: LabelledGeneric.Aux[D, R],
        fromDynamoDbItem: FromDynamoDbItem[R]
    ): DynamoDb[D] = new DynamoDb[D] {

        val ddb = JavaDynamoDbClient.builder()
            .region(region)
            .build()

        def load(id: String): IO[D] = for {
            result <- IO(
                ddb.getItem(
                    GetItemRequest
                        .builder()
                        .tableName(table)
                        .key(Map("id" -> AttributeValue.builder().s(id).build()).asJava)
                        .build()
                )
            )
            item = result.item().asScala.toMap
            parsed <- IO.fromEither(FromDynamoDbItem.from(item))
        } yield parsed

        def save(d: D): IO[Unit] = for {
            _ <- IO(
                ddb.putItem(
                    PutItemRequest.builder()
                        .item(???)
                        .build()
                )
            )
        } yield ()
    }
}