package emergencymanager.backend.dynamodb

import emergencymanager.commons.data.FoodItem

import emergencymanager.backend.dynamodb._
import emergencymanager.backend.dynamodb.implicits._

import emergencymanager.backend.data._

import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should._

import cats.effect.IO

import shapeless.record._

import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.regions.Region

class DynamoDbSpec extends AnyFlatSpec with Matchers {

  implicit val region = Region.EU_CENTRAL_1

  type RecordType = Record.`"author" -> String, "title" -> String, "id" -> Int`.T

  "The construction of a DynamoDb instance using the FoodItem.OldUserItem type" should "compile" in {
    """val db: DynamoDb[IO, FoodItem.OldUserItem] = DynamoDb.io("Foo")""" should compile
  }

  "The construction of a DynamoDb instance using the Token type" should "compile" in {
    """val db: DynamoDb[IO, Token] = DynamoDb.io("Foo")""" should compile
  }

  "The construction of a DynamoDb instance using the User type" should "compile" in {
    """val db: DynamoDb[IO, User] = DynamoDb.io("Foo")""" should compile
  }

  "A query containing two 'contains' statements" should "be constructed correctly" in {

    val contains1 = Query[RecordType].contains["author"]("Benjamin")
    val contains2 = Query[RecordType].contains["title"]("Programming")
    val composite = contains1 and contains2
    val result = composite.build

    result._2 shouldBe("contains (#key0, :var0) and contains (#key1, :var1)")
    result._3 should contain (":var0" -> AttributeValue.builder().s("Benjamin").build())
    result._3 should contain (":var1" -> AttributeValue.builder().s("Programming").build())
    result._4 should contain ("#key0" -> "author")
    result._4 should contain ("#key1" -> "title")
  }

  "A 'contains' query containing an non-existent field" should "not typecheck" in {
    """val contains = Query[RecordType].contains["auther"]("Benjamin")""" shouldNot typeCheck
  }

  "A 'contains' query containing an non-string field" should "not typecheck" in {
    """val contains = Query[RecordType].contains["id"]("Benjamin")""" shouldNot typeCheck
  }
}
