package emergencymanager.backend.database

import org.bson.BsonValue

trait FromBsonValue[A] {
    def apply(bson: BsonValue): ParseResult[A]
}