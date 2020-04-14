package emergencymanager.backend

import emergencymanager.backend.data._

import emergencymanager.backend.database._
import emergencymanager.backend.database.implicits._

import cats.implicits._
import cats.effect._

import shapeless.{Id => _, _}
import shapeless.record._
import shapeless.syntax.singleton._

import org.mongodb.scala.MongoDatabase

import scala.util.Random

import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import java.util.Base64

trait UserService[F[_]] {
    def maxTokenAgeSeconds: Long
    def login(id: String, password: String): F[Option[String]]
    def challenge(tokenId: String): F[Option[String]]
}

object UserService {

    val hashingAlgorithm = MessageDigest.getInstance("SHA-512")

    def apply[F[_]](implicit u: UserService[F]): UserService[F] = u

    implicit def userServiceIo(implicit
        clock: Clock[IO],
        concurrentEffect: ConcurrentEffect[IO],
        database: MongoDatabase
    ): UserService[IO] = new UserService[IO] {

        val userDb = Collection[User]("users")
        val tokenDb = Collection[Token]("tokens")
        
        val maxTokenAgeSeconds: Long = 24 * 60 * 60

        def login(id: String, password: String): IO[Option[String]] = userDb
            .findOption(Query[userDb.WithId].equals["_id"](id))
            .flatMap( _
                .filter(comparePassword(password) _)
                .traverse(createToken _)
            )

        def challenge(tokenId: String): IO[Option[String]] = tokenDb
            .findOption(Query[tokenDb.WithId].equals["_id"](tokenId))
            .flatMap(optToken => clock.realTime(TimeUnit.SECONDS)
                .map(time => optToken
                    .filter(token => token("expires") > time)
                )
            )
            .flatMap(
                _.traverse(token =>
                    updateExpiration(token)
                        .as(token("userId"))
                )
            )

        private def createToken(user: userDb.WithId): IO[String] = {
            val tokenValue = randomTokenValue

            clock.realTime(TimeUnit.SECONDS)
                .flatMap( time =>
                    tokenDb.save(
                        ("userId" ->> user("_id")) ::
                        ("expires" ->> (time + maxTokenAgeSeconds)) ::
                        HNil
                    )
                )
                .as(tokenValue)
        }

        private def updateExpiration(token: tokenDb.WithId): IO[Unit] =
            clock.realTime(TimeUnit.SECONDS)
                .map(time => token + ("expires" ->> (time + maxTokenAgeSeconds)))
                .flatMap(tokenDb.overwrite)

        private def comparePassword(password: String)(user: userDb.WithId): Boolean = {
            val providedPasswordHash = hashPassword(password.getBytes, user("salt"))

            val base64hash = new String(Base64.getEncoder.encode(providedPasswordHash.toArray))
            println("Hash: " + base64hash)

            providedPasswordHash equals user("passwordHash")
        }

        private def randomTokenValue: String =
            Random.alphanumeric.take(32).mkString

        private def hashPassword(password: Array[Byte], salt: Array[Byte]): Array[Byte] = 
            hashingAlgorithm.digest(password ++ salt)
    }
}