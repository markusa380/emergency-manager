package backend

import backend.model._
import backend.programs._

import cats.effect._
import cats.implicits._
import cats.syntax._

import shapeless._

import org.http4s.HttpRoutes
import org.http4s.syntax._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze._

import software.amazon.awssdk.regions.Region

object Application extends IOApp {

  val suppliesStorage = SuppliesStorage.apply(Region.EU_CENTRAL_1)

  val httpRoutes = SuppliesController.httpRoutes(suppliesStorage)
    .orElse(
      HttpRoutes.of[IO] {
        case GET -> Root / "health" =>
          Ok("Healthy")  
      }
    )
    .orNotFound
  
  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(httpRoutes)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
