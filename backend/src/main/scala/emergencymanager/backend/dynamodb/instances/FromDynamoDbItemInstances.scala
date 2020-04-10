package emergencymanager.backend.dynamodb.instances

import emergencymanager.backend.dynamodb._

import cats.implicits._

import shapeless._
import shapeless.labelled.{ FieldType, field }

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

trait FromDynamoDbItemInstances {

    implicit val hnilFromDynamoDbItem: FromDynamoDbItem[HNil] = new FromDynamoDbItem[HNil] {
        def apply(m: Map[String, AttributeValue]): ParseResult[HNil] = Right(HNil)
    }

    implicit def hConsFromDynamoDbItem[HeadLabel <: String : ValueOf, HeadValue, Tail <: HList](implicit
        valueFromAttributeValue: FromAttributeValue[HeadValue],
        tailFromDynamoDbItem: Lazy[FromDynamoDbItem[Tail]]
    ): FromDynamoDbItem[FieldType[HeadLabel, HeadValue] :: Tail] = new FromDynamoDbItem[FieldType[HeadLabel, HeadValue] :: Tail] {
        def apply(m: Map[String, AttributeValue]): ParseResult[FieldType[HeadLabel, HeadValue] :: Tail] = {
            val keyName = valueOf[HeadLabel]
            for {
                rawValue <- m.get(valueOf[HeadLabel])
                    .toRight(ParseFailure(s"Key $keyName does not exist in item: $m"))
                parsedValue <- valueFromAttributeValue.apply(rawValue)
                parsedTail <- tailFromDynamoDbItem.value(m)
            } yield field[HeadLabel](parsedValue) :: parsedTail
        }
    }

    implicit def hConsFromDynamoDbItemOption[HeadLabel <: String : ValueOf, HeadValue, Tail <: HList](implicit
        valueFromAttributeValue: FromAttributeValue[HeadValue],
        tailFromDynamoDbItem: Lazy[FromDynamoDbItem[Tail]]
    ): FromDynamoDbItem[FieldType[HeadLabel, Option[HeadValue]] :: Tail] = new FromDynamoDbItem[FieldType[HeadLabel, Option[HeadValue]] :: Tail] {
        def apply(m: Map[String, AttributeValue]): ParseResult[FieldType[HeadLabel, Option[HeadValue]] :: Tail] = {

            for {
                parsedValue <- m.get(valueOf[HeadLabel])
                    .traverse(valueFromAttributeValue.apply)
                parsedTail <- tailFromDynamoDbItem.value(m)
            } yield field[HeadLabel](parsedValue) :: parsedTail
        }
    }
}