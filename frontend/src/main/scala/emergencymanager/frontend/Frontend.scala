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
        handler <- Handler.create[Unit].toIO
        node = div(
            p("Hello World"),
            button("Click me", onClick.use(()) --> handler),
            p(handler.scan(0)((count, _) => count + 1).map("Button was clicked " + _ + " times"))
        )
        _ <- OutWatch.renderInto[IO]("#app", node)
        exitCode = ExitCode.Success
    } yield exitCode
}