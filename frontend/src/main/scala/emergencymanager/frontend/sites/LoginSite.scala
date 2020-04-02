package emergencymanager.frontend.sites

import emergencymanager.frontend.Client
import emergencymanager.frontend.Dom._

import cats.implicits._
import cats.effect._

import outwatch._
import outwatch.dsl._
import outwatch.reactive.handler._
import colibri._

object LoginSite {

    def create(exitObserver: Observer[Unit])(implicit client: Client[IO]): IO[VNode] = for {

        usernameHandler   <- Handler.createF[IO, String]("")
        passwordHandler   <- Handler.createF[IO, String]("")
        doLoginHandler    <- Handler.createF[IO, Unit]

        usernamePassword = Observable.combineLatest(usernameHandler, passwordHandler)

        loginObservable = doLoginHandler
            .async
            .withLatestMap(usernamePassword)((_, userPass) => userPass)
            .concatMapAsync { case (user, pass) => client.login(user, pass).attempt }

        failedLogin = loginObservable
            .mapFilter(_.left.toOption)

        dom = container(
            card(
                styles.marginTop := "2em",
                cardBody(
                    cardTitle("Please login"),
                    formGroup(
                        textInput(
                            placeholder := "Username",
                            onInput.value --> usernameHandler,
                            formControlled
                        )
                    ),
                    formGroup(
                        passwordInput(
                            placeholder := "Password",
                            onInput.value --> passwordHandler,
                            onKeyUp.filter(_.key.equalsIgnoreCase("Enter")).use(()) --> doLoginHandler,
                            formControlled
                        )
                    ),
                    formGroup(
                        primaryButton("Login", onMouseDown.use(()) --> doLoginHandler),
                        failedLogin
                            .map(t => p(styles.color.red, s"Login failed: ${t.getMessage}"))
                    )
                )
            )
        )
        // We don't need to provide any information on the user
        // for now, as the stored cookie is enough
        _ <- IO(loginObservable.as(()) subscribe exitObserver)
    } yield dom

}