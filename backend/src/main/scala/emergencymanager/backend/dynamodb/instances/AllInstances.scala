package emergencymanager.backend.dynamodb.instances

trait AllInstances
    extends ToAttributeValueInstances
    with FromAttributeValueInstances
    with ToDynamoDbItemInstances
    with FromDynamoDbItemInstances