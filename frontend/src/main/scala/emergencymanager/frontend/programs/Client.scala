package emergencymanager.frontend

import emergencymanager.commons.data.Supplies
import emergencymanager.commons.data.Auth

import cats.implicits._
import cats.effect._

import outwatch.util.Http
import colibri.Observable

import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.parser.decode

import scala.concurrent.ExecutionContext
import org.scalajs.dom.ext.AjaxException

object Client {

    def challenge(implicit ctx: ContextShift[IO]): IO[Boolean] = Http
        .single(
            Http.Request.apply(
                url = "/api/challenge"
            ),
            Http.Get
        )
        .adaptErr { case e: AjaxException => new Exception(e.xhr.responseText) }
        .map(_.response.asInstanceOf[String].toBoolean)

    def login(username: String, password: String)(implicit ctx: ContextShift[IO]): IO[Unit] = Http
        .single(
            Http.Request(
                url = "/api/login",
                data = Auth(username, password)
                    .asJson
                    .spaces2
            ),
            Http.Post
        )
        .adaptErr { case e: AjaxException => new Exception(e.xhr.responseText) }
        .as(())

    def loadSupplies(implicit ctx: ContextShift[IO]): IO[List[Supplies]] = Http
        .single(
            Http.Request.apply(
                url = "/api/supplies"
            ),
            Http.Get
        )
        .adaptErr { case e: AjaxException => new Exception(e.xhr.responseText) }
        .map(_.response.asInstanceOf[String])
        .flatMap(json => IO.fromEither(decode[List[Supplies]](json)))

    def createItem(supplies: Supplies)(implicit ctx: ContextShift[IO]): IO[Unit] = Http
        .single(
            Http.Request(
                url = "/api/supplies",
                data = supplies
                    .asJson
                    .spaces2
            ),
            Http.Post
        )
        .adaptErr { case e: AjaxException => new Exception(e.xhr.responseText) }
        .as(())

    def retrieveItem(id: String)(implicit ctx: ContextShift[IO]): IO[Supplies] = Http
        .single(
            Http.Request(
                url = "/api/supplies/single?id=" + id
            ),
            Http.Get
        )
        .adaptErr { case e: AjaxException => new Exception(e.xhr.responseText) }
        .map(_.response.asInstanceOf[String])
        .flatMap(json => IO.fromEither(decode[Supplies](json)))
}