package emergencymanager.backend.database

import org.bson.BsonDocument

trait ToBsonDocument[A] {
    def apply(a: A): BsonDocument
}

object ToBsonDocument {
    def apply[A](implicit t: ToBsonDocument[A]) = t
}