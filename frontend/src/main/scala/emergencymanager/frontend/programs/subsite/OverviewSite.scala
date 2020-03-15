package emergencymanager.frontend.programs.subsite

import emergencymanager.frontend.Client
import emergencymanager.frontend.Dom._

import cats.syntax._
import cats.implicits._
import cats.effect._

import outwatch._
import outwatch.dsl.{col => _, _}
import outwatch.reactive.handler._
import colibri._

case class OverviewSite(
    dom: VNode,
    onCreate: Observable[Unit]
)

object OverviewSite {

    def create(
        editHandler: Observer[String]
    )(
        implicit ctx: ContextShift[IO]
    ): SyncIO[OverviewSite] = for {
        createHandler <- Handler.create[Unit]
        reloadHandler <- Handler.create[Unit]
    } yield {

        val supplies = Observable
            .merge(
                reloadHandler
            )
            .startWith(List(()))
            .concatMapAsync(_ => Client.loadSupplies)

        val dom = container(
            h1(
                styles.marginTop := "1em",
                "Emergency Supplies Manager"
            ),
            row(
                col(10)(
                    h3(
                        Client.sumCalories
                            .map(d => s"Your supplies are worth ${d.toInt} kcal / ${(d / 2500.0).toInt} person-days.")
                    )
                ),
                col(
                    primaryButton("Create", onMouseDown.use(()) --> createHandler)
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
                            val bbd = s.bestBefore
                                .map(_.toString)
                                .getOrElse("")

                            tr(
                                td(primaryButton("Edit"), onMouseDown.use(s.id) --> editHandler),
                                td(s.name),
                                td(bbd),
                                td(s.kiloCalories.toString + " kcal"),
                                td(s.weightGrams.toString + " g"),
                                td(s.number)
                            )
                        }
                    )
                )
            )
        )

        OverviewSite(dom, createHandler)
    }
}