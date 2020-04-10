package emergencymanager.backend.dynamodb

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

trait ToAttributeValue[T] {
    def apply(t: T): AttributeValue
}

object ToAttributeValue {
    def to[A](a: A)(implicit to: ToAttributeValue[A]) = to(a)
}