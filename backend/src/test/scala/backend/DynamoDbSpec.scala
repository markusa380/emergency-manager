package emergencymanager.backend

import emergencymanager.commons.data.FoodItem

import emergencymanager.backend.dynamodb._
import emergencymanager.backend.dynamodb.implicits._

import emergencymanager.backend.data._

import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should._

import cats.effect.IO

import software.amazon.awssdk.regions.Region

class DynamoDbSpec extends AnyFlatSpec with Matchers {

  implicit val region = Region.EU_CENTRAL_1

  "The construction of a DynamoDb instance using the FoodItem.UserItem type" should "compile" in {
    """val db: DynamoDb[IO, FoodItem.UserItem] = DynamoDb.io("Foo")""" should compile
  }

  "The construction of a DynamoDb instance using the Token type" should "compile" in {
    """val db: DynamoDb[IO, Token] = DynamoDb.io("Foo")""" should compile
  }

  "The construction of a DynamoDb instance using the User type" should "compile" in {
    """val db: DynamoDb[IO, User] = DynamoDb.io("Foo")""" should compile
  }
}
