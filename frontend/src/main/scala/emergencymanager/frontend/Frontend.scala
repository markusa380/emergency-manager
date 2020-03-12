package emergencymanager.frontend

import cats.implicits._
import cats.effect._

import outwatch._
import outwatch.dsl._
import outwatch.reactive.handler._

import monix.execution.Scheduler.Implicits.global

import scala.concurrent.duration._

import emergencymanager.commons.data.Supplies

object Frontend extends IOApp {

    def run(args: List[String]): IO[ExitCode] = for {
        dom <- app
        _   <- renderDom(dom)
    } yield (ExitCode.Success)

    def renderDom(dom: VNode): IO[Unit] =
        OutWatch.renderInto[IO]("#app", dom)

    val app: IO[VNode] = for {
        handler <- Handler.create[Unit].toIO
    } yield {
        div(
            p("Hello World"),
            button("Click me", onClick.use(()) --> handler),
            p(handler.scan(0)((count, _) => count + 1).map("Button was clicked " + _ + " times"))
        )
    }
}