package emergencymanager.frontend.programs

import cats.implicits._
import cats.effect._

import outwatch._
import outwatch.dsl._

import outwatch.reactive.handler._
import colibri._

import scala.concurrent.duration._
import emergencymanager.frontend.Client

object Login {

    def loginOr(dom: IO[VNode], resetLogin: Observable[Unit])(implicit ctx: ContextShift[IO]): IO[VNode] = for {
        usernameHandler     <- Handler.create[String].toIO
        passwordHandler     <- Handler.create[String].toIO
        doLoginHandler      <- Handler.create[Unit].toIO
    } yield {

        val usernamePasswordObservable = Observable.combineLatest(usernameHandler, passwordHandler)

        val loggedInObservable = Observable.merge(
            Observable(()).concatMapAsync(_ => Client.challenge),
            doLoginHandler
                .withLatest(usernamePasswordObservable)
                .map(_._2)
                .concatMapAsync { case (user, pass) => Client.login(user, pass).as(true) },
            resetLogin.as(false)
        )

        div(
            loggedInObservable.map {
                case false  => IO(
                    div(
                        cls := "container",
                        div(
                            cls := "card",
                            p("Please login:"),
                            input(textInput, placeholder := "Username", onInput.value --> usernameHandler),
                            input(passwordInput, placeholder := "Password", onInput.value --> passwordHandler),
                            button("Login", onClick.use(()) --> doLoginHandler, cls := "btn btn-primary")
                        )
                    )
                )
                case true => dom
            }
        )
    }
}