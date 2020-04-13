package emergencymanager.backend.database.instances

import emergencymanager.commons.data.IdField
import emergencymanager.backend.database._

import cats.implicits._

import shapeless._
import shapeless.labelled._

import org.bson.BsonDocument

trait FromBsonDocumentInstances {

    implicit val hnilFromBsonDocument: FromBsonDocument[HNil] = new FromBsonDocument[HNil] {
        def apply(document: BsonDocument): ParseResult[HNil] = Right(HNil)
    }

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
    ): FromBsonDocument[FieldType["_id", IdField] :: Tail] = new FromBsonDocument[FieldType["_id", IdField] :: Tail] {
        def apply(document: BsonDocument): ParseResult[FieldType["_id", IdField] :: Tail] = {

            for {
                parsedValue <- Option(document.get("_id"))
                    .toRight(ParseFailure(s"Key _id does not exist in document: $document"))
                    .map(_.asObjectId.getValue.toHexString)
                    .map(IdField.apply)
                parsedTail <- tailFromDocument.value.apply(document)
            } yield field["_id"](parsedValue) :: parsedTail
        }
    }

}