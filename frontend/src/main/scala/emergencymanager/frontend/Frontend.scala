package emergencymanager.frontend

import cats.implicits._
import cats.effect._

import outwatch.dom._
import outwatch.dom.dsl._

import monix.execution.Scheduler.Implicits.global

import scala.concurrent.duration._

object Frontend extends IOApp {

    def run(args: List[String]): IO[ExitCode] = for {
        handler <- Handler.create[String]
        node = p("Hello World")
        _ <- OutWatch.renderInto("#app", node)
    } yield ExitCode.Success
}