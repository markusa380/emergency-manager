package backend.algebra

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

import scala.jdk.CollectionConverters._

case class ToDynamoDbItemFailure(message: String) extends Exception(message)

trait ToAttributeValue[T] {
    def apply(t: T): AttributeValue
}

trait LowPriorityToAttributeValue {

    implicit def objectListToAttributeValue[T](implicit
        toAttributeValue: ToAttributeValue[T]
    ): ToAttributeValue[List[T]] = new ToAttributeValue[List[T]] {
        def apply(list: List[T]): AttributeValue = AttributeValue.builder()
            .l(list.map(toAttributeValue.apply).asJavaCollection)
            .build()
    }
}

object ToAttributeValue extends LowPriorityFromAttributeValue {

    implicit val booleanToAttributeValue: ToAttributeValue[Boolean] = new ToAttributeValue[Boolean] {
        def apply(t: Boolean): AttributeValue = AttributeValue.builder()
            .bool(t)
            .build()
    }

    implicit def numericToAttributeValue[N](implicit num: Numeric[N]): ToAttributeValue[N] = new ToAttributeValue[N] {
        def apply(t: N): AttributeValue = AttributeValue.builder()
            .n(num.toDouble(t).toString)
            .build()
    }

    implicit val stringToAttributeValue: ToAttributeValue[String] = new ToAttributeValue[String] {
        def apply(t: String): AttributeValue = AttributeValue.builder()
            .s(t)
            .build()
    }

    implicit val stringListToAttributeValue: ToAttributeValue[List[String]] = new ToAttributeValue[List[String]] {
        def apply(t: List[String]): AttributeValue = AttributeValue.builder()
            .ss(t.asJavaCollection)
            .build()
    }
}

trait ToDynamoDbItem[D] {
    def apply(d: D): Either[ToDynamoDbItemFailure, Map[String, AttributeValue]]
}