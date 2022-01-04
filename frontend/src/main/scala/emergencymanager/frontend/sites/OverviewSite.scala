package emergencymanager.frontend.sites

import emergencymanager.commons.implicits._

import emergencymanager.frontend.Client
import emergencymanager.frontend.Dom._

import cats.implicits._
import cats.effect._

import shapeless.record._

import outwatch._
import outwatch.dsl.{col => _, _}
import colibri._

import scala.concurrent.duration._

object OverviewSite {

    val searchDebounce = 500.millis
    val caloriesPerPersonDay = 2500.0

    def create(
        editObserver: Observer[String],
        createObserver: Observer[Unit]
    )(
        implicit client: Client[IO]
    ): IO[VNode] = for {
        _ <- IO.unit

        // ### Handlers ### //
        searchStringHandler = Subject.publish[String]
        clearHandler = Subject.publish[Unit]

        // ### Observables ### //

        supplies = Observable
            .merge(
                searchStringHandler
                    .debounce(searchDebounce),
                clearHandler.map(_ => "")
            )
            .startWith("".pure[List])
            .mapAsync(name => client.searchItems(name))

        // ### DOM ### //

        dom = container(
            h1(
                styles.marginTop := "1em",
                "Emergency Supplies Manager"
            ),
            row(
                col(10)(
                    h3(
                        client.sumCalories
                            .map(d => s"Your supplies are worth ${d.toInt} kcal / ${(d / caloriesPerPersonDay).toInt} person-days.")
                    )
                )
            ),
            row(
                col(
                    formInline(
                        textInput(
                            placeholder := "Search...",
                            value <-- clearHandler.map(_ => ""),
                            onInput.value --> searchStringHandler,
                            formControlled
                        ),
                        secondaryButton("Clear", onMouseDown.use(()) --> clearHandler, marginX(2), formControlled),
                        primaryButton("Create", onMouseDown.use(()) --> createObserver, formControlled),
                        width := "100%"
                    )
                )
            ),
            table(
                cls := "table",
                styles.width := "100%",
                styles.marginTop := "1em",
                thead(
                    tr(
                        th("", attr("scope") := "col"),
                        th("Name", attr("scope") := "col"),
                        th("Best Before Date", attr("scope") := "col"),
                        th("Calories / 100g", attr("scope") := "col"),
                        th("Weight (g)", attr("scope") := "col"),
                        th("#", attr("scope") := "col")
                    )
                ),
                tbody(
                    supplies.map(list =>
                        list.map { s =>
                            val bbd = s("bestBefore")
                                .map(_.mkString)
                                .getOrElse("")

                            tr(
                                td(primaryButton("Edit"), onMouseDown.use(s("_id")) --> editObserver, verticalAlign.middle),
                                td(s("name"), verticalAlign.middle),
                                td(bbd, verticalAlign.middle),
                                td(s("kiloCalories").toString + " kcal", verticalAlign.middle),
                                td(s("weightGrams").toString + " g", verticalAlign.middle),
                                td(s("number"), verticalAlign.middle)
                            )
                        }
                    )
                )
            )
        )
    } yield dom
}