package emergencymanager.frontend.sites

import emergencymanager.commons.data.Auth
import emergencymanager.commons.implicits._

import emergencymanager.frontend.Client
import emergencymanager.frontend.Dom._

import cats.implicits._
import cats.effect._

import shapeless.syntax.std.tuple._

import outwatch._
import outwatch.dsl._
import colibri._

object LoginSite {

    def create(exitObserver: Observer[Unit])(implicit client: Client[IO]): IO[VNode] = for {

        _ <- IO.unit
        usernameHandler   = Subject.behavior("")
        passwordHandler   = Subject.behavior("")
        doLoginHandler    = Subject.publish[Unit]

        usernamePassword = Observable.combineLatest(usernameHandler, passwordHandler)

        loginObservable = doLoginHandler
            .async
            .withLatestMap(usernamePassword)((_, userPass) => userPass)
            .map(_.productElements)
            .map(_.mapToRecord[Auth])
            .mapAsync(auth => client.login(auth).attempt)
            .publish

        failedLogin = loginObservable
            .mapFilter(_.left.toOption)

        successfulLogin = loginObservable
            .mapFilter(_.toOption)
            .as(())

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
        _ <- IO(loginObservable.connect())
        _ <- IO(successfulLogin subscribe exitObserver)
    } yield dom

}