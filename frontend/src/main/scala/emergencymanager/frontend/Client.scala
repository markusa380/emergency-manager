package emergencymanager.frontend

import emergencymanager.commons.data._

import cats.implicits._
import cats.effect._

import outwatch.util.Http

import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.shapes._

import org.scalajs.dom.ext.AjaxException

trait Client[F[_]] {
    def challenge: F[Boolean]
    def login(auth: Auth): F[Unit]
    def loadSupplies: F[List[FoodItem.IdItem]]
    def createItem(item: FoodItem.NewItem): F[Unit]
    def editItem(item: FoodItem.IdItem): F[Unit]
    def deleteItem(itemId: String): F[Unit]
    def sumCalories: F[Double]
    def retrieveItem(itemId: String): F[FoodItem.IdItem]
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
        
        def login(auth: Auth): IO[Unit] = Http
            .single(
                Http.Request(
                    url = baseUrl + "/api/login",
                    data = auth
                        .asJson
                        .spaces2
                ),
                Http.Post
            )
            .adaptErr { case e: AjaxException => new Exception(e.xhr.responseText) }
            .as(())
        
        def loadSupplies: IO[List[FoodItem.IdItem]] = Http
            .single(
                Http.Request.apply(
                    url = baseUrl + "/api/supplies"
                ),
                Http.Get
            )
            .adaptErr { case e: AjaxException => new Exception(e.xhr.responseText) }
            .map(_.response.asInstanceOf[String])
            .flatMap(json => IO.fromEither(decode[List[FoodItem.IdItem]](json)))
        
        def createItem(item: FoodItem.NewItem): IO[Unit] = Http
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

        def editItem(item: FoodItem.IdItem): IO[Unit] = Http
            .single(
                Http.Request(
                    url = baseUrl + "/api/supplies/update",
                    data = item
                        .asJson
                        .spaces2
                ),
                Http.Post
            )
            .adaptErr { case e: AjaxException => println(e.xhr.responseText); new Exception(e.xhr.statusText + " " + e.xhr.responseText) }
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
        
        def retrieveItem(itemId: String): IO[FoodItem.IdItem] = Http
            .single(
                Http.Request(
                    url = baseUrl + "/api/supplies/single?id=" + itemId
                ),
                Http.Get
            )
            .adaptErr { case e: AjaxException => new Exception(e.xhr.responseText) }
            .map(_.response.asInstanceOf[String])
            .flatMap(json => IO.fromEither(decode[FoodItem.IdItem](json)))
        
    }
}