package emergencymanager.backend.database

import emergencymanager.backend.database.implicits._
import emergencymanager.commons.data._

import cats.implicits._
import cats.effect._

import shapeless.{Id => _, _}

import org.mongodb.scala._
import org.bson.BsonDocument

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

    def apply[D <: HList : ToBsonDocument : FromBsonDocument](
        collectionName: String
    )(implicit
        db: MongoDatabase,
        ce: ConcurrentEffect[IO],
        ctx: ContextShift[IO]
    ): Collection[IO, D] = new Collection[IO, D] {

        val collection = db.getCollection[BsonDocument](collectionName)

        def save(document: D): IO[Unit] = collection
            .insertOne(document.toBsonDocument)
            .toIO
            .map(_ => println(s"Stored one new document to collection $collectionName"))

        def overwrite(document: WithId): IO[Unit] = collection
            .replaceOne(
                Query[WithId].idEquals(document.head).build,
                document.toBsonDocument
            )
            .toIO
            .map(result => println(s"Overwrite modified ${result.getModifiedCount} elements in collection $collectionName"))
            .as(())

        def list: IO[List[WithId]] = collection
            .find()
            .collect()
            .toIO
            .flatMap( _
                .toList
                .traverse(doc =>
                    IO.fromEither(
                        FromBsonDocument[WithId].apply(doc)
                    )
                )
            )

        def find(query: Query[WithId]): IO[List[WithId]] = collection
            .find(query.build)
            .collect()
            .toIO
            .map { seq => println(s"Query found ${seq.size} results."); seq }
            .flatMap( _
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
            .limit(1)
            .collect()
            .toIO
            .flatMap( _
                .headOption
                .traverse(doc =>
                    IO.fromEither(
                        FromBsonDocument[WithId].apply(doc)
                    )
                )
            )

        def deleteOne(query: Query[WithId]): IO[Unit] = collection
            .deleteOne(query.build)
            .toIO
            .as(())
    }
    
}