package emergencymanager.backend.database.instances

import emergencymanager.backend.database._

import emergencymanager.commons.data.IdField

import shapeless._
import shapeless.labelled.FieldType

import org.bson._

import scala.jdk.CollectionConverters._
import org.bson.types.ObjectId

trait ToBsonDocumentInstances {

    implicit val hnilToBsonDocument: ToBsonDocument[HNil] = new ToBsonDocument[HNil] {
        def apply(a: HNil): BsonDocument = new BsonDocument()
    }

    implicit def hConsToBsonDocument[HeadName <: String : ValueOf, HeadValue, Tail <: HList](implicit
        headToBsonValue: ToBsonValue[HeadValue],
        tailToBsonDocument: Lazy[ToBsonDocument[Tail]]
    ): ToBsonDocument[FieldType[HeadName, HeadValue] :: Tail] = new ToBsonDocument[FieldType[HeadName, HeadValue] :: Tail] {
        def apply(a: FieldType[HeadName,HeadValue] :: Tail): BsonDocument =
            tailToBsonDocument
                .value
                .apply(a.tail)
                .entrySet
                .asScala
                .map(entry => (entry.getKey, entry.getValue))
                .foldLeft(new BsonDocument)((doc, entry) => doc.append(entry._1, entry._2))
                .append(valueOf[HeadName], headToBsonValue(a.head))
    }

    implicit def hConsOptionToBsonDocument[HeadName <: String : ValueOf, HeadValue, Tail <: HList](implicit
        headToBsonValue: ToBsonValue[HeadValue],
        tailToBsonDocument: Lazy[ToBsonDocument[Tail]]
    ): ToBsonDocument[FieldType[HeadName, Option[HeadValue]] :: Tail] = new ToBsonDocument[FieldType[HeadName, Option[HeadValue]] :: Tail] {
        def apply(a: FieldType[HeadName, Option[HeadValue]] :: Tail): BsonDocument = {
            
            val tailSet = tailToBsonDocument
                .value
                .apply(a.tail)
                .entrySet
                .asScala
                .map(entry => (entry.getKey, entry.getValue))

            val headSet = a.head.map(value => (valueOf[HeadName], headToBsonValue(value))).toSet
        
            (headSet ++ tailSet)
                .foldLeft(new BsonDocument)((doc, entry) => doc.append(entry._1, entry._2))
        }
    }

    implicit def hConsIdToBsonDocument[Tail <: HList](implicit
        tailToBsonDocument: Lazy[ToBsonDocument[Tail]]
    ): ToBsonDocument[FieldType["_id", IdField] :: Tail] = new ToBsonDocument[FieldType["_id", IdField] :: Tail] {
        def apply(a: FieldType["_id", IdField] :: Tail): BsonDocument = {
            tailToBsonDocument
                .value
                .apply(a.tail)
                .entrySet
                .asScala
                .map(entry => (entry.getKey, entry.getValue))
                .foldLeft(new BsonDocument)((doc, entry) => doc.append(entry._1, entry._2))
                .append("_id", new BsonObjectId(new ObjectId(a.head.id)))
        }
    }
}