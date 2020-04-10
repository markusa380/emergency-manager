package emergencymanager.backend

import emergencymanager.backend.dynamodb._
import emergencymanager.backend.data.{User, Token}

import cats.implicits._
import cats.effect._

import shapeless._
import shapeless.record._
import shapeless.syntax.singleton._

import java.security.MessageDigest
import java.util.concurrent.TimeUnit

trait UserService[F[_]] {
    def maxTokenAgeSeconds: Long
    def login(id: String, password: String): F[Option[String]]
    def challenge(tokenId: String): F[Option[String]]
}

object UserService {
    def apply[F[_]](implicit u: UserService[F]): UserService[F] = u

    implicit def userServiceIo(implicit
        clock: Clock[IO],
        userDb: DynamoDb[IO, User],
        tokenDb: DynamoDb[IO, Token]
    ): UserService[IO] = new UserService[IO] {
        
        val maxTokenAgeSeconds: Long = 24 * 60 * 60

        def login(id: String, password: String): IO[Option[String]] = userDb
            .loadOption(id)
            .flatMap(
                _.filter(comparePassword(password) _)
                .traverse(createToken _)
            )

        def challenge(tokenId: String): IO[Option[String]] = tokenDb
            .loadOption(tokenId)
            .flatMap(optToken => clock.realTime(TimeUnit.SECONDS)
                .map(time => optToken
                    .filter(token => token("expires") > time) // This should be redundant if TTL is activated in DynamoDB
                )
            )
            .flatMap(
                _.traverse(token =>
                    updateExpiration(token)
                        .as(token("userId"))
                )
            )

        private def createToken(user: User): IO[String] = {
            val tokenValue = randomTokenValue

            clock.realTime(TimeUnit.SECONDS)
                .flatMap( time =>
                    tokenDb.save(
                        ("id" ->> tokenValue) ::
                        ("userId" ->> user("id")) ::
                        ("expires" ->> (time + maxTokenAgeSeconds)) ::
                        HNil
                    )
                )
                .as(tokenValue)
        }

        private def updateExpiration(token: Token): IO[Unit] =
            clock.realTime(TimeUnit.SECONDS)
                .map(time =>
                    token + ("expires" ->> (time + maxTokenAgeSeconds))
                )
                .flatMap(tokenDb.save)

        private def comparePassword(password: String)(user: User): Boolean = {
            val providedPasswordHash = hashPassword(password.getBytes.toList, user("salt"))

            val base64hash = new String(java.util.Base64.getEncoder.encode(providedPasswordHash.toArray))
            println("Hash: " + base64hash)

            providedPasswordHash equals user("passwordHash")
        }

        private def randomTokenValue: String = {
            scala.util.Random.alphanumeric.take(32).mkString
        }

        private def hashPassword(password: List[Byte], salt: List[Byte]): List[Byte] = {
            val md = MessageDigest.getInstance("SHA-512")
            val saltedPassword = password ++ salt
            
            md.digest(saltedPassword.toArray).toList
        }
    }
}