package emergencymanager.backend.algebra.serde.dynamodb

import cats.implicits._

import shapeless._
import shapeless.labelled.{ FieldType, field }

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

trait FromDynamoDbItem[Result] {
    def apply(item: Map[String, AttributeValue]): ParseResult[Result]
}

object FromDynamoDbItem {

    implicit def reprFromDynamoDbItem[T, R <: HList](implicit
        labelledGeneric: LabelledGeneric.Aux[T, R],
        fromDynamoDbItem: FromDynamoDbItem[R]
    ): FromDynamoDbItem[T] = new FromDynamoDbItem[T] {
        def apply(item: Map[String,AttributeValue]): ParseResult[T] =
            fromDynamoDbItem(item).map(labelledGeneric.from)
    }

    implicit val hnilFromDynamoDbItem: FromDynamoDbItem[HNil] = new FromDynamoDbItem[HNil] {
        def apply(m: Map[String, AttributeValue]): ParseResult[HNil] = Right(HNil)
    }

    implicit def hConsFromDynamoDbItem[HeadLabel <: Symbol, HeadValue, Tail <: HList](implicit
        keyNameWitness: Witness.Aux[HeadLabel],
        valueFromAttributeValue: FromAttributeValue[HeadValue],
        tailFromDynamoDbItem: Lazy[FromDynamoDbItem[Tail]]
    ): FromDynamoDbItem[FieldType[HeadLabel, HeadValue] :: Tail] = new FromDynamoDbItem[FieldType[HeadLabel, HeadValue] :: Tail] {
        def apply(m: Map[String, AttributeValue]): ParseResult[FieldType[HeadLabel, HeadValue] :: Tail] = {
            val keyName = keyNameWitness.value.name
            for {
                rawValue <- m.get(keyNameWitness.value.name)
                    .toRight(ParseFailure(s"Key $keyName does not exist in item: $m"))
                parsedValue <- valueFromAttributeValue.apply(rawValue)
                parsedTail <- tailFromDynamoDbItem.value(m)
            } yield field[HeadLabel](parsedValue) :: parsedTail
        }
    }

    implicit def hConsFromDynamoDbItemOption[HeadLabel <: Symbol, HeadValue, Tail <: HList](implicit
        keyNameWitness: Witness.Aux[HeadLabel],
        valueFromAttributeValue: FromAttributeValue[HeadValue],
        tailFromDynamoDbItem: Lazy[FromDynamoDbItem[Tail]]
    ): FromDynamoDbItem[FieldType[HeadLabel, Option[HeadValue]] :: Tail] = new FromDynamoDbItem[FieldType[HeadLabel, Option[HeadValue]] :: Tail] {
        def apply(m: Map[String, AttributeValue]): ParseResult[FieldType[HeadLabel, Option[HeadValue]] :: Tail] = {

            for {
                parsedValue <- m.get(keyNameWitness.value.name)
                    .traverse(valueFromAttributeValue.apply)
                parsedTail <- tailFromDynamoDbItem.value(m)
            } yield field[HeadLabel](parsedValue) :: parsedTail
        }
    }
}