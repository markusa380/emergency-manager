package emergencymanager.backend.algebra.serde.dynamodb

import scala.jdk.CollectionConverters._

import cats.implicits._

import shapeless._
import shapeless.labelled.{ FieldType, field }

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

trait FromAttributeValue[A] {
    def apply(av: AttributeValue): ParseResult[A]
}

trait LowPriorityFromAttributeValue {

    // This needs to be low priority so a List[String] isn't converted to nested AttributeValues
    // but instead uses the instance stringArrayFromAttributeValue
    implicit def objectListFromAttributeValue[A](implicit
        aFromAttributeValue: FromAttributeValue[A]
    ): FromAttributeValue[List[A]] = new FromAttributeValue[List[A]] {
        def apply(av: AttributeValue): ParseResult[List[A]] =
            Option(av.l()) // Can be null, so wrap
                .toRight(ParseFailure(s"AttributeValue does not contain L: $av"))
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
        def apply(av: AttributeValue): ParseResult[A] = for {
            javaMap <- Option(av.m())
                .toRight(ParseFailure(s"Attribute does not contain a nested item: $av"))
            map = javaMap.asScala.toMap
            res <- fromDynamoDbItem(map)
        } yield labelledGeneric.from(res)
    }

    implicit val boolFromAttributeValue: FromAttributeValue[Boolean] = new FromAttributeValue[Boolean] {
        def apply(av: AttributeValue): ParseResult[Boolean] =
            Option(av.bool()) // Can be null, so wrap
                .toRight(ParseFailure(s"AttributeValue does not contain BOOL: $av"))
                .map(_.booleanValue) // Scala's `Boolean` is the same as Java's `boolean`
    }

    implicit val stringFromAttributeValue: FromAttributeValue[String] = new FromAttributeValue[String] {
        def apply(av: AttributeValue): ParseResult[String] =
            Option(av.s()) // Can be null, so wrap
                .toRight(ParseFailure(s"AttributeValue does not contain S: $av"))
    }

    implicit def stringArrayFromAttributeValue: FromAttributeValue[List[String]] = new FromAttributeValue[List[String]] {
        def apply(av: AttributeValue): ParseResult[List[String]] =
            Option(av.ss()) // Can be null, so wrap
                .toRight(ParseFailure(s"AttributeValue does not contain SS: $av"))
                .map(_.asScala.toList) // Convert to Scala List
    }

    implicit def numericFromAttributeValue[A](
        implicit num: Numeric[A]
    ): FromAttributeValue[A] = new FromAttributeValue[A] {
        def apply(av: AttributeValue): ParseResult[A] = Option(av.n()) // Can be null, so wrap
            .toRight(ParseFailure(s"AttributeValue does not contain N: $av"))
            .flatMap(n =>
                num.parseString(n) // Try to parse
                    .toRight(ParseFailure(s"Could not parse number: $n"))
            )
    }
}
