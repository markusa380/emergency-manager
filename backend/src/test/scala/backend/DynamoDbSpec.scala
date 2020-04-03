package emergencymanager.backend

import emergencymanager.backend.programs.DynamoDb

import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should._

import cats.effect.IO

import software.amazon.awssdk.regions.Region
import java.{util => ju}

import emergencymanager.commons.data.FoodItem

class DynamoDbSpec extends AnyFlatSpec with Matchers {

  implicit val region = Region.EU_CENTRAL_1

  "The construction of a DynamoDb instance using the FoodItem case-class" should "compile" in {
    """val db: DynamoDb[IO, FoodItem] = DynamoDb.io("Foo")""" should compile
  }

  "The construction of a DynamoDb instance using a complicated case class" should "compile" in {
    case class Bar(str: String)
    case class Foo(str: String, num: BigDecimal, opt: Option[String], list: List[String], item: Bar, optItem: Option[Bar])
    """val db: DynamoDb[IO, Foo] = DynamoDb.io("Foo")""" should compile
  }

  "The construction of a DynamoDb instance using a case class that contains an unknown type" should "not typecheck" in {
    case class Foo(date: ju.Date)
    """val db: DynamoDb[IO, Foo] = DynamoDb.io("Foo")""" shouldNot typeCheck
  }
}
