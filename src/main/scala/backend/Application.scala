package backend

import backend.model._
import backend.programs._

import cats.effect._
import cats.implicits._

import shapeless._

import org.http4s.HttpRoutes
import org.http4s.syntax._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze._

import software.amazon.awssdk.regions.Region

object Application extends IOApp {

    val dynamoDb: DynamoDb[Supplies] = DynamoDb.apply(Region.EU_CENTRAL_1, "Supplies")
    
    val backendService = HttpRoutes.of[IO] {
        case GET -> Root / "test" =>
          Ok()
        
      }.orNotFound
    
      def run(args: List[String]): IO[ExitCode] =
        dynamoDb.delete("1234") *>
        dynamoDb.save(Supplies("1234", "Tomatosauce", BestBeforeDate(Some(1), Some(12), 2020), 500, 500, 3)) *>
        dynamoDb.load("1234").flatMap(s => IO(println(s))) *>
        BlazeServerBuilder[IO]
          .bindHttp(8080, "localhost")
          .withHttpApp(backendService)
          .serve
          .compile
          .drain
          .as(ExitCode.Success)
}
