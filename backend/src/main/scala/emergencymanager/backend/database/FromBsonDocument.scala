package emergencymanager.backend.database

import org.bson.BsonDocument

trait FromBsonDocument[D] {
    def apply(document: BsonDocument): ParseResult[D]
}

object FromBsonDocument {
    def apply[A](implicit f: FromBsonDocument[A]) = f
}