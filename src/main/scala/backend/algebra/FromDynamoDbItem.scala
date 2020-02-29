package backend.algebra

import cats.implicits._

import shapeless._, labelled.{ FieldType, field }

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

import scala.jdk.CollectionConverters._

case class FromDynamoDbItemFailure(message: String) extends Exception(message)

trait FromAttributeValue[A] {
    def apply(av: AttributeValue): Option[A]
}

trait LowPriorityFromAttributeValue {

    // This probably needs to be low priority because it could collide with stringArrayFromAttributeValue
    implicit def objectListFromAttributeValue[A](implicit
        aFromAttributeValue: FromAttributeValue[A]
    ): FromAttributeValue[List[A]] = new FromAttributeValue[List[A]] {
        def apply(av: AttributeValue): Option[List[A]] =
            Option(av.l()) // Can be null, so wrap
                .map(_.asScala.toList) // Convert wrapped Java - List to Scala - List
                .flatMap(
                    // Only return `Some` if all elements can be parsed (using `traverse`)
                    _.traverse(aFromAttributeValue.apply)
                )
    }
}

object FromAttributeValue extends LowPriorityFromAttributeValue {

    implicit val boolFromAttributeValue: FromAttributeValue[Boolean] = new FromAttributeValue[Boolean] {
        def apply(av: AttributeValue): Option[Boolean] = Option(av.bool()) // Can be null, so wrap
            .map(_.booleanValue) // Scala's `Boolean` is the same as Java's `boolean`
    }

    implicit val stringFromAttributeValue: FromAttributeValue[String] = new FromAttributeValue[String] {
        def apply(av: AttributeValue): Option[String] = Option(av.s()) // Can be null, so wrap
    }

    implicit def stringArrayFromAttributeValue: FromAttributeValue[List[String]] = new FromAttributeValue[List[String]] {
        def apply(av: AttributeValue): Option[List[String]] = Option(av.ss()) // Can be null, so wrap
            .map(_.asScala.toList) // Convert to Scala List
    }

    implicit def numericFromAttributeValue[A](
        implicit num: Numeric[A]
    ): FromAttributeValue[A] = new FromAttributeValue[A] {
        def apply(av: AttributeValue): Option[A] = Option(av.n()) // Can be null, so wrap
            .flatMap(num.parseString) // Try to parse
    }

    implicit def optionFromAttributeValue[A](implicit
        aFromAttributeValue: FromAttributeValue[A]
    ): FromAttributeValue[Option[A]] = new FromAttributeValue[Option[A]] {
        def apply(av: AttributeValue): Option[Option[A]] =
            Some(aFromAttributeValue.apply(av)) // If an Option is expected it is always successful TODO: Maybe not?
    }
}

trait FromDynamoDbItem[Result <: HList] {
    def apply(item: Map[String, AttributeValue]): Either[FromDynamoDbItemFailure, Result]
}

trait LowPriorityFromDynamoDbItem {

    implicit def hConsFromDynamoDbItem1[HeadKey <: Symbol, HeadValue, Tail <: HList](implicit
        keyNameWitness: Witness.Aux[HeadKey],
        valueFromAttributeValue: FromAttributeValue[HeadValue],
        tailFromDynamoDbItem: Lazy[FromDynamoDbItem[Tail]]
    ): FromDynamoDbItem[FieldType[HeadKey, HeadValue] :: Tail] = new FromDynamoDbItem[FieldType[HeadKey, HeadValue] :: Tail] {
        def apply(m: Map[String, AttributeValue]): Either[FromDynamoDbItemFailure, FieldType[HeadKey, HeadValue] :: Tail] = {
            val keyName = keyNameWitness.value.name
            for {
                rawValue <- m.get(keyNameWitness.value.name)
                    .toRight(FromDynamoDbItemFailure(s"Key $keyName does not exist in item: $m"))
                parsedValue <- valueFromAttributeValue.apply(rawValue)
                    .toRight(FromDynamoDbItemFailure(s"Failed to parse $keyName. Raw: ${rawValue.toString}"))
                parsedTail <- tailFromDynamoDbItem.value(m)
            } yield field[HeadKey](parsedValue) :: parsedTail
        }
    }
}

object FromDynamoDbItem extends LowPriorityFromDynamoDbItem {
    
    /**
     * Helper method to convert DynamoDB items to arbitrary case classes
     */
    def from[A, Repr <: HList](m: Map[String, AttributeValue])(implicit
        gen: LabelledGeneric.Aux[A, Repr],
        fromDynamoDbItem: FromDynamoDbItem[Repr]
    ): Either[FromDynamoDbItemFailure, A] = fromDynamoDbItem(m).map(gen.from(_))

    implicit val hnilFromDynamoDbItem: FromDynamoDbItem[HNil] = new FromDynamoDbItem[HNil] {
        def apply(m: Map[String, AttributeValue]): Either[FromDynamoDbItemFailure, HNil] = Right(HNil)
    }

    // I think we need this implementation and not just the low-priority one
    // because LabelledGeneric does not convert nested case classes.
    implicit def hConsFromDynamoDbItem0[HeadKey <: Symbol, HeadValue, HeadValueRepr <: HList, Tail <: HList](implicit
        keyNameWitness: Witness.Aux[HeadKey],
        gen: LabelledGeneric.Aux[HeadValue, HeadValueRepr],
        valueFromDynamoDbItem: FromDynamoDbItem[HeadValueRepr],
        tailFromDynamoDbItem: FromDynamoDbItem[Tail] // Why does this not need to be Lazy?
    ): FromDynamoDbItem[FieldType[HeadKey, HeadValue] :: Tail] = new FromDynamoDbItem[FieldType[HeadKey, HeadValue] :: Tail] {
        def apply(m: Map[String, AttributeValue]): Either[FromDynamoDbItemFailure, FieldType[HeadKey, HeadValue] :: Tail] = {
            val keyName = keyNameWitness.value.name
            for {
                rawValue <- m.get(keyNameWitness.value.name)
                        .toRight(FromDynamoDbItemFailure(s"Key $keyName does not exist in item: $m"))
                nestedMap <- Option(rawValue.m())
                    .toRight(FromDynamoDbItemFailure(s"Key $keyName did not contain a nested item"))
                parsedValue <- valueFromDynamoDbItem(rawValue.m().asScala.toMap)
                parsedTail <- tailFromDynamoDbItem(m)
            } yield field[HeadKey](gen.from(parsedValue)) :: parsedTail
        }
    }
}