package backend.algebra

import cats.implicits._

import shapeless._, labelled.{ FieldType, field }

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

import scala.jdk.CollectionConverters._

case class FromFailure(message: String) extends Exception(message)

trait FromAttributeValue[A] {
    def apply(av: AttributeValue): Either[FromFailure, A]
}

trait LowPriorityFromAttributeValue {

    // This needs to be low priority so a List[String] isn't converted to nested AttributeValues
    // but instead uses the instance stringArrayFromAttributeValue
    implicit def objectListFromAttributeValue[A](implicit
        aFromAttributeValue: FromAttributeValue[A]
    ): FromAttributeValue[List[A]] = new FromAttributeValue[List[A]] {
        def apply(av: AttributeValue): Either[FromFailure, List[A]] =
            Option(av.l()) // Can be null, so wrap
                .toRight(FromFailure(s"AttributeValue does not contain L: $av"))
                .map(_.asScala.toList) // Convert wrapped Java - List to Scala - List
                .flatMap(
                    // Only return `Some` if all elements can be parsed (using `traverse`)
                    _.traverse(aFromAttributeValue.apply)
                )
    }
}

object FromAttributeValue extends LowPriorityFromAttributeValue {

    implicit def objectFromAttributeValue[A, R <: HList](implicit
        labelledGeneric: LabelledGeneric.Aux[A, R],
        fromDynamoDbItem: FromDynamoDbItem[R]
    ): FromAttributeValue[A] = new FromAttributeValue[A] {
        def apply(av: AttributeValue): Either[FromFailure, A] = for {
            javaMap <- Option(av.m())
                .toRight(FromFailure(s"Attribute does not contain a nested item: $av"))
            map = javaMap.asScala.toMap
            res <- fromDynamoDbItem(map)
        } yield labelledGeneric.from(res)
    }

    implicit val boolFromAttributeValue: FromAttributeValue[Boolean] = new FromAttributeValue[Boolean] {
        def apply(av: AttributeValue): Either[FromFailure, Boolean] =
            Option(av.bool()) // Can be null, so wrap
                .toRight(FromFailure(s"AttributeValue does not contain BOOL: $av"))
                .map(_.booleanValue) // Scala's `Boolean` is the same as Java's `boolean`
    }

    implicit val stringFromAttributeValue: FromAttributeValue[String] = new FromAttributeValue[String] {
        def apply(av: AttributeValue): Either[FromFailure, String] =
            Option(av.s()) // Can be null, so wrap
                .toRight(FromFailure(s"AttributeValue does not contain S: $av"))
    }

    implicit def stringArrayFromAttributeValue: FromAttributeValue[List[String]] = new FromAttributeValue[List[String]] {
        def apply(av: AttributeValue): Either[FromFailure, List[String]] =
            Option(av.ss()) // Can be null, so wrap
                .toRight(FromFailure(s"AttributeValue does not contain SS: $av"))
                .map(_.asScala.toList) // Convert to Scala List
    }

    implicit def numericFromAttributeValue[A](
        implicit num: Numeric[A]
    ): FromAttributeValue[A] = new FromAttributeValue[A] {
        def apply(av: AttributeValue): Either[FromFailure, A] = Option(av.n()) // Can be null, so wrap
            .toRight(FromFailure(s"AttributeValue does not contain N: $av"))
            .flatMap(n =>
                num.parseString(n) // Try to parse
                    .toRight(FromFailure(s"Could not parse number: $n"))
            )
    }

    implicit def optionFromAttributeValue[A](implicit
        aFromAttributeValue: FromAttributeValue[A]
    ): FromAttributeValue[Option[A]] = new FromAttributeValue[Option[A]] {
        def apply(av: AttributeValue): Either[FromFailure, Option[A]] =
            if (av.nul()) Right(None)
            else aFromAttributeValue.apply(av).map(_.some)
    }
}

trait FromDynamoDbItem[Result <: HList] {
    def apply(item: Map[String, AttributeValue]): Either[FromFailure, Result]
}

object FromDynamoDbItem {
    
    /**
     * Helper method to convert DynamoDB items to arbitrary case classes
     */
    def from[A, Repr <: HList](m: Map[String, AttributeValue])(implicit
        gen: LabelledGeneric.Aux[A, Repr],
        fromDynamoDbItem: FromDynamoDbItem[Repr]
    ): Either[FromFailure, A] = fromDynamoDbItem(m).map(gen.from(_))

    implicit val hnilFromDynamoDbItem: FromDynamoDbItem[HNil] = new FromDynamoDbItem[HNil] {
        def apply(m: Map[String, AttributeValue]): Either[FromFailure, HNil] = Right(HNil)
    }

    implicit def hConsFromDynamoDbItem1[HeadKey <: Symbol, HeadValue, Tail <: HList](implicit
        keyNameWitness: Witness.Aux[HeadKey],
        valueFromAttributeValue: FromAttributeValue[HeadValue],
        tailFromDynamoDbItem: Lazy[FromDynamoDbItem[Tail]]
    ): FromDynamoDbItem[FieldType[HeadKey, HeadValue] :: Tail] = new FromDynamoDbItem[FieldType[HeadKey, HeadValue] :: Tail] {
        def apply(m: Map[String, AttributeValue]): Either[FromFailure, FieldType[HeadKey, HeadValue] :: Tail] = {
            val keyName = keyNameWitness.value.name
            for {
                rawValue <- m.get(keyNameWitness.value.name)
                    .toRight(FromFailure(s"Key $keyName does not exist in item: $m"))
                parsedValue <- valueFromAttributeValue.apply(rawValue)
                parsedTail <- tailFromDynamoDbItem.value(m)
            } yield field[HeadKey](parsedValue) :: parsedTail
        }
    }
}