package emergencymanager.backend.database

object implicits
    extends instances.FromBsonValueInstances
    with instances.FromBsonDocumentInstances
    with instances.ToBsonDocumentInstances
    with instances.ToBsonValueInstances
    with ops.BsonDocumentOps
    with ops.MongoObservableOps