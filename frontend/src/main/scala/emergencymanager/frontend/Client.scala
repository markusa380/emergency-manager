package emergencymanager.frontend

import emergencymanager.commons.data.Supplies
import emergencymanager.commons.data.Auth

import cats.implicits._
import cats.effect._

import outwatch.util.Http

import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.parser.decode

import org.scalajs.dom.ext.AjaxException

trait Client[F[_]] {
    def challenge: F[Boolean]
    def login(username: String, password: String): F[Unit]
    def loadSupplies: F[List[Supplies]]
    def createItem(item: Supplies): F[Unit]
    def deleteItem(itemId: String): F[Unit]
    def sumCalories: F[Double]
    def retrieveItem(itemId: String): F[Supplies]
}

object Client {

    val baseUrl = ""

    def apply[F[_]](implicit c: Client[F]): Client[F] = c

    implicit def ioClient(implicit ctx: ContextShift[IO]): Client[IO] = new Client[IO] {
        
        def challenge: IO[Boolean] = Http
            .single(
                Http.Request.apply(
                    url = baseUrl + "/api/challenge"
                ),
                Http.Get
            )
            .adaptErr { case e: AjaxException => new Exception(e.xhr.responseText) }
            .map(_.response.asInstanceOf[String].toBoolean)
        
        def login(username: String, password: String): IO[Unit] = Http
            .single(
                Http.Request(
                    url = baseUrl + "/api/login",
                    data = Auth(username, password)
                        .asJson
                        .spaces2
                ),
                Http.Post
            )
            .adaptErr { case e: AjaxException => new Exception(e.xhr.responseText) }
            .as(())
        
        def loadSupplies: IO[List[Supplies]] = Http
            .single(
                Http.Request.apply(
                    url = baseUrl + "/api/supplies"
                ),
                Http.Get
            )
            .adaptErr { case e: AjaxException => new Exception(e.xhr.responseText) }
            .map(_.response.asInstanceOf[String])
            .flatMap(json => IO.fromEither(decode[List[Supplies]](json)))
        
        def createItem(item: Supplies): IO[Unit] = Http
            .single(
                Http.Request(
                    url = baseUrl + "/api/supplies",
                    data = item
                        .asJson
                        .spaces2
                ),
                Http.Post
            )
            .adaptErr { case e: AjaxException => new Exception(e.xhr.statusText + " " + e.xhr.responseText) }
            .as(())
        
        def deleteItem(itemId: String): IO[Unit] = Http
            .single(
                Http.Request(
                    url = baseUrl + "/api/supplies?id=" + itemId
                ),
                Http.Delete
            )
            .adaptErr { case e: AjaxException => new Exception(e.xhr.statusText + " " + e.xhr.responseText) }
            .as(())
        
        def sumCalories: IO[Double] = Http
            .single(
                Http.Request(
                    url = baseUrl + "/api/supplies/calories"
                ),
                Http.Get
            )
            .adaptErr { case e: AjaxException => new Exception(e.xhr.statusText + " " + e.xhr.responseText) }
            .map(_.response.asInstanceOf[String].toDouble)
        
        def retrieveItem(itemId: String): IO[Supplies] = Http
            .single(
                Http.Request(
                    url = baseUrl + "/api/supplies/single?id=" + itemId
                ),
                Http.Get
            )
            .adaptErr { case e: AjaxException => new Exception(e.xhr.responseText) }
            .map(_.response.asInstanceOf[String])
            .flatMap(json => IO.fromEither(decode[Supplies](json)))
        
    }
}