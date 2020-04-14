package emergencymanager.backend

import emergencymanager.backend.data.ApplicationConf

import cats.effect._

import pureconfig._
import pureconfig.generic.auto._

import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.MongoClient

import scala.io.Source
import java.io.File

object ApplicationUtil {

    def loadApplicationConf = IO.fromEither(
        ConfigSource
            .default
            .load[ApplicationConf]
            .left.map(_.toList.map(_.description).mkString("; "))
            .left.map(error => new Exception(s"Failed to load app configuration: $error"))
    )

    def loadSecret(path: String, name: String) = IO(
        Source
            .fromFile(path + File.separatorChar + name)
            .getLines()
            .next()
    )

    def createMongoDatabase(config: ApplicationConf): IO[MongoDatabase] = {
        loadSecret(config.secretsPath, config.mongodbPasswordFile)
            .map(password =>
                MongoClient
                    .apply(s"mongodb://${config.mongodbUser}:${password}@${config.mongodbHost}:${config.mongodbPort}/?authSource=${config.mongodbDb}&readPreference=primary&ssl=false")
                    .getDatabase(config.mongodbDb)
            )
    }
}