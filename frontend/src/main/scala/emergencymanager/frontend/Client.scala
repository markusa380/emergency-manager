package emergencymanager.frontend

import emergencymanager.commons.data.Supplies
import emergencymanager.commons.data.Auth

import cats.implicits._
import cats.effect._

import outwatch.util.Http
import colibri.Observable

import io.circe.syntax._
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext

object Client {

    val baseUrl = "localhost"

    def login(username: String, password: String)(implicit ctx: ContextShift[IO]): IO[Unit] = Http
        .single(
            Http.Request.apply(
                url = baseUrl,
                data = Auth(username, password)
                    .asJson
                    .spaces2
            ),
            Http.Post
        )
        .as(())

    // def loadSupplies: IO[List[Supplies]]
}