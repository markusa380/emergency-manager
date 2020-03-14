package emergencymanager.frontend.programs.subsite

import cats.syntax._
import cats.implicits._
import cats.effect._

import outwatch._
import outwatch.dsl._
import outwatch.reactive.handler._
import colibri._

import emergencymanager.frontend.Client

case class LoginSite(
    dom: VNode,
    onLogin: Observable[Unit]
)

object LoginSite {
    def create(
        implicit ctx: ContextShift[IO]
    ): SyncIO[LoginSite] = for {
        usernameInput   <- Handler.create[String]
        passwordInput   <- Handler.create[String]
        loginHandler    <- Handler.create[Either[Throwable, Unit]]
    } yield {

        val usernamePassword = Observable.combineLatest(usernameInput, passwordInput)

        val loginOnClick = onClick.use(())
            .async
            .useLatest(usernamePassword)
            .concatMapAsync { case (user, pass) => Client.login(user, pass).attempt }
            .map {
                e =>
                    println(e)
                    e
            }

        val failedLogin = loginHandler
            .mapFilter(_.left.toOption)

        val vnode = div(
            cls := "container",
            div(
                cls := "card",
                styles.marginTop := "2em",
                div(
                    cls := "card-body",
                    h5(cls := "card-title", "Please login"),
                    div(
                        cls := "form-group",
                        input(`type` := "text", placeholder := "Username", cls := "form-control", onInput.value --> usernameInput)
                    ),
                    div(
                        cls := "form-group",
                        input(`type` := "password", placeholder := "Password", cls := "form-control", onInput.value --> passwordInput)
                    ),
                    div(
                        cls := "form-group",
                        button("Login", loginOnClick --> loginHandler, cls := "btn btn-primary"),
                        failedLogin
                            .map(t => p(styles.color.red, s"Login failed: ${t.getMessage}"))
                    )
                )
            )
        )

        LoginSite(vnode, loginHandler.mapFilter(_.toOption))
    }
        
}