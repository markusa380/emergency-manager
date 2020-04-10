package emergencymanager.backend.dynamodb

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

trait FromDynamoDbItem[Result] {
    def apply(item: Map[String, AttributeValue]): ParseResult[Result]
}