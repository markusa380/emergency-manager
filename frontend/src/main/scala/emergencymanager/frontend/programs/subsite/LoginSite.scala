package emergencymanager.frontend.programs.subsite

import emergencymanager.frontend.Client
import emergencymanager.frontend.Dom._

import cats.syntax._
import cats.implicits._
import cats.effect._

import outwatch._
import outwatch.dsl._
import outwatch.reactive.handler._
import colibri._

case class LoginSite(
    dom: VNode,
    onLogin: Observable[Unit]
)

object LoginSite {
    def create(
        implicit ctx: ContextShift[IO]
    ): SyncIO[LoginSite] = for {
        usernameHandler   <- Handler.create[String]("")
        passwordHandler   <- Handler.create[String]("")
        doLoginHandler    <- Handler.create[Unit]
    } yield {

        val usernamePassword = Observable.combineLatest(usernameHandler, passwordHandler)

        val loginObservable = doLoginHandler
            .async
            .withLatestMap(usernamePassword)((_, userPass) => userPass)
            .concatMapAsync { case (user, pass) => Client.login(user, pass).attempt }

        val failedLogin = loginObservable
            .mapFilter(_.left.toOption)

        val vnode = container(
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

        LoginSite(vnode, loginObservable.mapFilter(_.toOption))
    }
        
}