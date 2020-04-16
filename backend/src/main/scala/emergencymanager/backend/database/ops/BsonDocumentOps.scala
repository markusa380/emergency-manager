package emergencymanager.backend.database.ops

import emergencymanager.backend.database._
import org.bson.BsonDocument

trait BsonDocumentOps {

    implicit class ToBsonDocumentOps[A : ToBsonDocument](a: A) {
        def toBsonDocument: BsonDocument = ToBsonDocument[A].apply(a)
    }
}