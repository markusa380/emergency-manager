package emergencymanager.backend.algebra.serde.dynamodb

import cats.implicits._

import shapeless._
import shapeless.labelled.{ field, FieldType }

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

import scala.jdk.CollectionConverters._

trait ToDynamoDbItem[A] {
    def apply(a: A): Map[String, AttributeValue]
}

object ToDynamoDbItem {

    implicit def genericToDynamoDbItem[A, Repr <: HList](implicit
        lgen: LabelledGeneric.Aux[A, Repr],
        reprToDynamoDbItem: ToDynamoDbItem[Repr]
    ): ToDynamoDbItem[A] = new ToDynamoDbItem[A] {
        def apply(a: A): Map[String, AttributeValue] = reprToDynamoDbItem(lgen.to(a))
    }

    implicit val hnilToDynamoDbItem: ToDynamoDbItem[HNil] = new ToDynamoDbItem[HNil] {
        def apply(a: HNil): Map[String,AttributeValue] = Map.empty
    }

    implicit def hConsToDynamoDbItem[HeadName <: Symbol, HeadValue, Tail <: HList](implicit
        headNameWitness: Witness.Aux[HeadName],
        headValueToAttributeValue: ToAttributeValue[HeadValue],
        tailToDynamoDbItem: Lazy[ToDynamoDbItem[Tail]]
    ): ToDynamoDbItem[FieldType[HeadName, HeadValue] :: Tail] = new ToDynamoDbItem[FieldType[HeadName, HeadValue] :: Tail] {
        def apply(a: FieldType[HeadName,HeadValue] :: Tail): Map[String, AttributeValue] =
            tailToDynamoDbItem.value.apply(a.tail) + ((headNameWitness.value.name, headValueToAttributeValue(a.head)))
    }

    implicit def hConsWithOptionHeadToDynamoDbItem[HeadName <: Symbol, HeadValue, Tail <: HList](implicit
        headNameWitness: Witness.Aux[HeadName],
        headValueToAttributeValue: ToAttributeValue[HeadValue],
        tailToDynamoDbItem: Lazy[ToDynamoDbItem[Tail]]
    ): ToDynamoDbItem[FieldType[HeadName, Option[HeadValue]] :: Tail] = new ToDynamoDbItem[FieldType[HeadName, Option[HeadValue]] :: Tail] {
        def apply(a: FieldType[HeadName, Option[HeadValue]] :: Tail): Map[String, AttributeValue] =
            tailToDynamoDbItem.value.apply(a.tail) ++ a.head
                .map(value => (headNameWitness.value.name, headValueToAttributeValue(value)))
    }
}