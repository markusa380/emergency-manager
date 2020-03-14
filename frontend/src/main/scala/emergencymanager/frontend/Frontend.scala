package emergencymanager.frontend

import emergencymanager.commons.data._
import emergencymanager.frontend.programs._
import emergencymanager.frontend.data._

import cats.implicits._
import cats.effect._

import outwatch._
import outwatch.dsl._

import outwatch.reactive.handler._
import colibri._

import scala.concurrent.duration._

import scala.util.Random
import java.{util => ju}
import emergencymanager.frontend.programs.subsite.LoginSite


object Frontend extends IOApp {

    def run(args: List[String]): IO[ExitCode] = for {
        _   <- renderDom(app)
    } yield (ExitCode.Success)

    def renderDom(dom: VNode): IO[Unit] =
        OutWatch.renderInto[IO]("#app", dom)

    def app: VNode = div(
        for {
            changeModeHandler <- Handler.create[Mode]
            loginSite <- LoginSite.create
        } yield {

            val loggedIn: Observable[Boolean] =
                Observable.fromAsync(Client.challenge)

            val initialMode: Observable[Mode] = loggedIn.map {
                case true => OverviewMode
                case false => LoginMode
            }

            val mode = Observable.merge(
                initialMode,
                loginSite.onLogin,
                changeModeHandler
            )

            mode.map {
                case LoginMode => loginSite.dom
                case _ => div("Eh")
            }
        }
    )

    private def randomId = ju.UUID.randomUUID().toString()
}