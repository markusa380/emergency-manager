package emergencymanager.backend.dynamodb.instances

import emergencymanager.backend.dynamodb._

import shapeless._
import shapeless.labelled.FieldType

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

trait ToDynamoDbItemInstances {

    implicit val hnilToDynamoDbItem: ToDynamoDbItem[HNil] = new ToDynamoDbItem[HNil] {
        def apply(a: HNil): Map[String,AttributeValue] = Map.empty
    }

    implicit def hConsToDynamoDbItem[HeadName <: String : ValueOf, HeadValue, Tail <: HList](implicit
        headValueToAttributeValue: ToAttributeValue[HeadValue],
        tailToDynamoDbItem: Lazy[ToDynamoDbItem[Tail]]
    ): ToDynamoDbItem[FieldType[HeadName, HeadValue] :: Tail] = new ToDynamoDbItem[FieldType[HeadName, HeadValue] :: Tail] {
        def apply(a: FieldType[HeadName,HeadValue] :: Tail): Map[String, AttributeValue] =
            tailToDynamoDbItem.value.apply(a.tail) + ((valueOf[HeadName], headValueToAttributeValue(a.head)))
    }

    implicit def hConsWithOptionHeadToDynamoDbItem[HeadName <: String : ValueOf, HeadValue, Tail <: HList](implicit
        headValueToAttributeValue: ToAttributeValue[HeadValue],
        tailToDynamoDbItem: Lazy[ToDynamoDbItem[Tail]]
    ): ToDynamoDbItem[FieldType[HeadName, Option[HeadValue]] :: Tail] = new ToDynamoDbItem[FieldType[HeadName, Option[HeadValue]] :: Tail] {
        def apply(a: FieldType[HeadName, Option[HeadValue]] :: Tail): Map[String, AttributeValue] =
            tailToDynamoDbItem.value.apply(a.tail) ++ a.head
                .map(value => (valueOf[HeadName], headValueToAttributeValue(value)))
    }
}