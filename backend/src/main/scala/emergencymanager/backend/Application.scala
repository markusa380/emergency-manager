package emergencymanager.backend

import emergencymanager.backend.ApplicationUtil._
import emergencymanager.backend.controller._

import cats.effect._
import cats.implicits._

import org.http4s.implicits._
import org.http4s.server.blaze._

object Application extends IOApp {
  
  def run(args: List[String]): IO[ExitCode] =
    loadApplicationConf.flatMap(conf =>
      createMongoDatabase(conf).flatMap { mongoDb =>
        implicit val m = mongoDb

        val httpRoutes = (
          SuppliesController.httpRoutes <+>
          UserController.httpRoutes <+>
          FrontendController.httpRoutes
        ).orNotFound

        BlazeServerBuilder[IO]
          .bindHttp(8080, "0.0.0.0")
          .withHttpApp(httpRoutes)
          .serve
          .compile
          .drain
          .as(ExitCode.Success)
      }
    )
}
