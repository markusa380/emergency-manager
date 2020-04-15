package emergencymanager.backend.database

import emergencymanager.backend.database.implicits._
import emergencymanager.commons.data._

import cats.implicits._
import cats.effect._

import shapeless.{Id => _, _}
import shapeless.record._

import org.mongodb.scala._
import design.hamu.mongoeffect._
import org.bson.BsonDocument
import shapeless.ops.record.Selector

trait Collection[F[_], Doc <: HList] {

    type WithId = Id :: Doc

    def save(document: Doc): F[Unit]
    def overwrite(document: WithId): F[Unit]

    def list: F[List[WithId]]

    def find(query: Query[WithId]): F[List[WithId]]
    def findOne(query: Query[WithId]): F[WithId]
    def findOption(query: Query[WithId]): F[Option[WithId]]

    def deleteOne(query: Query[WithId]): F[Unit]
}

object Collection {

    type Aux[F[_], Doc <: HList, WithId0 <: HList] = Collection[F, Doc] { type WithId = WithId0 }

    def apply[D <: HList : ToBsonDocument : FromBsonDocument](collectionName: String)(implicit
        db: MongoDatabase,
        ce: ConcurrentEffect[IO],
        // selector: Selector.Aux[Id :: D, "_id", String]
    ): Collection[IO, D] = new Collection[IO, D] {

        val collection = db.getCollection[BsonDocument](collectionName)

        implicit val withIdToBsonDocument = ToBsonDocument[WithId]

        def save(document: D): IO[Unit] = collection
            .insertOne(document.toBsonDocument)
            .headF[IO]
            .map(_ => println(s"Stored one new document to collection $collectionName"))

        def overwrite(document: WithId): IO[Unit] = {

            val id: String = document.head
            val query = Query[WithId].idEquals(id)

            findOne(query)
                .flatMap(old =>
                    collection
                        .replaceOne(withIdToBsonDocument(old), withIdToBsonDocument(document))
                        .headF[IO]
                )
                .map(result => println(s"Overwrite modified ${result.getModifiedCount} elements in collection $collectionName"))
                .as(())
        }

        def list: IO[List[WithId]] = collection
            .find()
            .collect()
            .headF[IO]
            .flatMap(_
                .toList
                .traverse(doc =>
                    IO.fromEither(
                        FromBsonDocument[WithId].apply(doc)
                    )
                )
            )

        def find(query: Query[WithId]): IO[List[WithId]] = collection
            .find(query.build)
            .collect
            .headF[IO]
            .map { seq => println(s"Query found ${seq.size} results."); seq }
            .flatMap(_
                .toList
                .traverse(doc =>
                    IO.fromEither(
                        FromBsonDocument[WithId].apply(doc)
                    )
                )
            )

        def findOne(query: Query[WithId]): IO[WithId] = findOption(query)
            .map(_.toRight(new Exception(s"Document for query ${query.build} not found")))
            .flatMap(IO.fromEither)

        def findOption(query: Query[WithId]): IO[Option[WithId]] = collection
            .find(query.build)
            .headOptF[IO]
            .flatMap( _
                .traverse(doc =>
                    IO.fromEither(
                        FromBsonDocument[WithId].apply(doc)
                    )
                )
            )

        def deleteOne(query: Query[WithId]): IO[Unit] = collection
            .deleteOne(query.build)
            .headF[IO]
            .as(())
    }
    
}