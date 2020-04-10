package emergencymanager.backend.dynamodb

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

trait FromAttributeValue[A] {
    def apply(av: AttributeValue): ParseResult[A]
}
