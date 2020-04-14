package emergencymanager.backend.database.instances

import emergencymanager.backend.database._
import emergencymanager.commons.data._

import cats.implicits._

import shapeless.{Id => _, _}
import shapeless.labelled._

import org.bson.BsonDocument

trait LowPriorityFromBsonDocumentInstances {

    implicit def hConsFromBsonDocument[HeadLabel <: String : ValueOf, HeadValue, Tail <: HList](implicit
        valueFromBson: FromBsonValue[HeadValue],
        tailFromDocument: Lazy[FromBsonDocument[Tail]]
    ): FromBsonDocument[FieldType[HeadLabel, HeadValue] :: Tail] = new FromBsonDocument[FieldType[HeadLabel, HeadValue] :: Tail] {
        def apply(document: BsonDocument): ParseResult[FieldType[HeadLabel, HeadValue] :: Tail] = {
            val keyName = valueOf[HeadLabel]

            for {
                rawValue <- Option(document.get(valueOf[HeadLabel]))
                    .toRight(ParseFailure(s"Key $keyName does not exist in document: $document"))
                parsedValue <- valueFromBson.apply(rawValue)
                parsedTail <- tailFromDocument.value.apply(document)
            } yield field[HeadLabel](parsedValue) :: parsedTail
        }
    }
}

trait FromBsonDocumentInstances extends LowPriorityFromBsonDocumentInstances {

    implicit val hnilFromBsonDocument: FromBsonDocument[HNil] = new FromBsonDocument[HNil] {
        def apply(document: BsonDocument): ParseResult[HNil] = Right(HNil)
    }

    implicit def hConsHeadOptionFromBsonDocument[HeadLabel <: String : ValueOf, HeadValue, Tail <: HList](implicit
        valueFromBson: FromBsonValue[HeadValue],
        tailFromDocument: Lazy[FromBsonDocument[Tail]]
    ): FromBsonDocument[FieldType[HeadLabel, Option[HeadValue]] :: Tail] = new FromBsonDocument[FieldType[HeadLabel, Option[HeadValue]] :: Tail] {
        def apply(document: BsonDocument): ParseResult[FieldType[HeadLabel, Option[HeadValue]] :: Tail] = {

            for {
                parsedValue <- Option(document.get(valueOf[HeadLabel]))
                    .traverse(valueFromBson.apply)
                parsedTail <- tailFromDocument.value(document)
            } yield field[HeadLabel](parsedValue) :: parsedTail
        }
    }

    implicit def hConsIdFieldFromBsonDocument[Tail <: HList](implicit
        tailFromDocument: Lazy[FromBsonDocument[Tail]]
    ): FromBsonDocument[Id :: Tail] = new FromBsonDocument[Id :: Tail] {
        def apply(document: BsonDocument): ParseResult[Id :: Tail] = {

            for {
                parsedValue <- Option(document.get("_id"))
                    .toRight(ParseFailure(s"Key _id does not exist in document: $document"))
                    .map(_.asObjectId.getValue.toHexString)
                parsedTail <- tailFromDocument.value.apply(document)
            } yield field["_id"](parsedValue) :: parsedTail
        }
    }

}