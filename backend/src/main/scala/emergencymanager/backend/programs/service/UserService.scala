package emergencymanager.backend.programs.service

import emergencymanager.backend.programs.DynamoDb
import emergencymanager.backend.data.{User, Token}

import cats.implicits._
import cats.effect._
import cats._

import software.amazon.awssdk.regions.Region

import java.security.MessageDigest
import cats.data.OptionT
import java.util.concurrent.TimeUnit

class UserService(
    implicit
        region: Region,
        clock: Clock[IO]
) {
    val encoding = "utf-8"

    val maxTokenAgeSeconds: Long = 24 * 60 * 60

    val userDb = DynamoDb[User]("EMUser")
    val tokenDb = DynamoDb[Token]("EMToken")

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
                .filter(_.lastChallenged + maxTokenAgeSeconds > time)
            )
        )
        .flatMap(
            _.traverse(token =>
                updateLastChallenged(token)
                    .as(token.userId)
            )
        )

    private def createToken(user: User): IO[String] = {
        val tokenValue = randomTokenValue

        clock.realTime(TimeUnit.SECONDS)
            .flatMap(time => tokenDb
                .save(
                    Token(
                        id = tokenValue,
                        userId = user.id,
                        lastChallenged = time
                    )
                )
            )
            .as(tokenValue)
    }

    private def updateLastChallenged(token: Token): IO[Unit] =
        clock.realTime(TimeUnit.SECONDS)
            .map(time => token.copy(lastChallenged = time))
            .flatMap(tokenDb.save)

    private def comparePassword(password: String)(user: User): Boolean = {
        val providedPasswordHash = hashPassword(password.getBytes.toList, user.salt)

        val base64hash = new String(java.util.Base64.getEncoder.encode(providedPasswordHash.toArray))
        println("Hash: " + base64hash)

        providedPasswordHash equals user.passwordHash
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