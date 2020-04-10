package emergencymanager.backend.dynamodb

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

trait ToDynamoDbItem[A] {
    def apply(a: A): Map[String, AttributeValue]
}