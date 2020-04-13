package emergencymanager.backend.database

import org.bson.BsonValue

trait ToBsonValue[A] {
    def apply(a: A): BsonValue
}