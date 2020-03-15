package emergencymanager.frontend

import emergencymanager.commons.data._
import emergencymanager.frontend.programs._
import emergencymanager.frontend.data._
import emergencymanager.frontend.programs.subsite._

import cats.implicits._
import cats.effect._

import outwatch._
import outwatch.dsl._

import outwatch.reactive.handler._
import colibri._

import scala.concurrent.duration._

import scala.util.Random
import java.{util => ju}


object Frontend extends IOApp {

    def run(args: List[String]): IO[ExitCode] = for {
        _   <- renderDom(app)
    } yield (ExitCode.Success)

    def renderDom(dom: VNode): IO[Unit] =
        OutWatch.renderInto[IO]("#app", dom)

    def app: VNode = div(
        for {
            loginSite <- LoginSite.create
            editSite <- EditSite.create
            createSite <- CreateSite.create
            overviewSite <- OverviewSite.create(editSite.itemToEdit)
           
        } yield {

            val loggedIn: Observable[Boolean] =
                Observable.fromAsync(Client.challenge)

            val initialMode: Observable[Mode] = loggedIn.map {
                case true => OverviewMode
                case false => LoginMode
            }

            val mode: Observable[Mode] = Observable.merge(
                initialMode,
                loginSite.onLogin.as[Mode](OverviewMode),
                overviewSite.onCreate.as[Mode](CreateMode),
                editSite.itemToEdit.map(id => EditMode(id)),
                editSite.onExit.as[Mode](OverviewMode),
                createSite.onExit.as[Mode](OverviewMode)
            )

            mode.map {
                case LoginMode => loginSite.dom
                case OverviewMode => overviewSite.dom
                case EditMode(id) => editSite.dom
                case CreateMode => createSite.dom
            }
        }
    )
}