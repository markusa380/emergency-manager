package emergencymanager.frontend

import cats.implicits._
import cats.effect._

import outwatch._
import outwatch.dsl._

import outwatch.reactive.handler._
import colibri._

import scala.concurrent.duration._

import emergencymanager.commons.data.Supplies
import emergencymanager.commons.data.BestBeforeDate
import scala.util.Random
import java.{util => ju}
import emergencymanager.frontend.programs.Table
import emergencymanager.frontend.programs.Login

object Frontend extends IOApp {

    def run(args: List[String]): IO[ExitCode] = for {
        dom <- app
        _   <- renderDom(dom)
    } yield (ExitCode.Success)

    def renderDom(dom: VNode): IO[Unit] =
        OutWatch.renderInto[IO]("#app", dom)

    def app: IO[VNode] = for {
        resetLoginHandler <- Handler.create[Unit].toIO
        loginOrDom <- Login.loginOr(
            IO(Table.create),
            resetLoginHandler
        )
    } yield loginOrDom

    private def randomId = ju.UUID.randomUUID().toString()
}