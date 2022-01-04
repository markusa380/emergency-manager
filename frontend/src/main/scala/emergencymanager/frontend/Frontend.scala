package emergencymanager.frontend

import emergencymanager.frontend._
import emergencymanager.frontend.sites._

import cats.implicits._
import cats.effect._

import outwatch._
import outwatch.dsl._

import colibri._

object Frontend extends IOApp {

    def run(args: List[String]): IO[ExitCode] = for {
        _ <- renderDom(app)
    } yield (ExitCode.Success)

    def renderDom(dom: VNode): IO[Unit] =
        OutWatch.renderInto[IO]("#app", dom)

    def app: VNode = {
         val modeHandler = Subject.publish[Mode]
            val loggedIn: Observable[Boolean] =
                Observable.fromAsync(Client[IO].challenge)

            val initialMode: Observable[Mode] = loggedIn.map {
                case true => OverviewMode
                case false => LoginMode
            }

            val mode: Observable[Mode] = Observable.merge(
                modeHandler,
                initialMode
            )


            div(
                mode.mapAsync {
                    case LoginMode      => LoginSite.create(
                        modeHandler.contramap(_ => OverviewMode)
                    )
                    case OverviewMode   => OverviewSite.create(
                        modeHandler.contramap(t => EditMode(t)),
                        modeHandler.contramap(_ => CreateMode)
                    )
                    case EditMode(id)   => EditSite.create(
                        id,
                        modeHandler.contramap(_ => OverviewMode)
                    )
                    case CreateMode     => CreateSite.create(
                        modeHandler.contramap(_ => OverviewMode)
                    )
                }
            )
        }
}