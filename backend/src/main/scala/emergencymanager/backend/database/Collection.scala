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

    type Aux[F[_], Doc <: HList, WithId0 <: HList] = Collection[F, Doc] { type WithId = WithId0 }

    def apply[D <: HList : ToBsonDocument : FromBsonDocument](collectionName: String)(implicit
        db: MongoDatabase,
        ce: ConcurrentEffect[IO],
        ctx: ContextShift[IO]
    ): Collection[IO, D] = new Collection[IO, D] {

        val collection = db.getCollection[BsonDocument](collectionName)

        implicit val withIdToBsonDocument = ToBsonDocument[WithId]

        def save(document: D): IO[Unit] = IO
            .fromFuture(
                IO(
                    collection
                        .insertOne(document.toBsonDocument)
                        .toFuture()
                )
            )
            .map(_ => println(s"Stored one new document to collection $collectionName"))

        def overwrite(document: WithId): IO[Unit] = {

            val id: String = document.head
            val query = Query[WithId].idEquals(id)

            findOne(query)
                .flatMap(old =>
                    IO.fromFuture(
                        IO(
                            collection
                                .replaceOne(withIdToBsonDocument(old), withIdToBsonDocument(document))
                                .toFuture
                        )
                    )
                )
                .map(result => println(s"Overwrite modified ${result.getModifiedCount} elements in collection $collectionName"))
                .as(())
        }

        def list: IO[List[WithId]] = IO
            .fromFuture(
                IO(
                    collection
                        .find()
                        .collect()
                        .toFuture
                )
            )
            .flatMap(_
                .toList
                .traverse(doc =>
                    IO.fromEither(
                        FromBsonDocument[WithId].apply(doc)
                    )
                )
            )

        def find(query: Query[WithId]): IO[List[WithId]] = IO
            .fromFuture(
                IO(
                    collection
                        .find(query.build)
                        .collect
                        .toFuture
                )
            )
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

        def findOption(query: Query[WithId]): IO[Option[WithId]] = IO
            .fromFuture(
                IO(
                    collection
                        .find(query.build)
                        .headOption
                )
            )
            .to[IO]
            .flatMap( _
                .traverse(doc =>
                    IO.fromEither(
                        FromBsonDocument[WithId].apply(doc)
                    )
                )
            )

        def deleteOne(query: Query[WithId]): IO[Unit] = IO
            .fromFuture(
                IO(
                    collection
                        .deleteOne(query.build)
                        .toFuture
                )
            )
            .as(())
    }
    
}