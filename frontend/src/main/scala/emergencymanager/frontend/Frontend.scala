package emergencymanager.frontend

import cats.implicits._
import cats.effect._

import outwatch._
import outwatch.dsl._

// import outwatch.reactive.handlers.monix._
import outwatch.reactive.handler._

import colibri._

import scala.concurrent.duration._

import emergencymanager.commons.data.Supplies
import emergencymanager.commons.data.BestBeforeDate
import scala.util.Random
import java.{util => ju}

object Frontend extends IOApp {

    def run(args: List[String]): IO[ExitCode] = for {
        dom <- app
        _   <- renderDom(dom)
    } yield (ExitCode.Success)

    def renderDom(dom: VNode): IO[Unit] =
        OutWatch.renderInto[IO]("#app", dom)

    val app: IO[VNode] = for {
        nameHandler <-  Handler.create[String].toIO
        bestBeforeDayHandler <- Handler.create[Option[Int]].toIO
        bestBeforeMonthHandler <- Handler.create[Option[Int]].toIO
        bestBeforeYearHandler <- Handler.create[Option[Int]].toIO
        kiloCaloriesHandler <- Handler.create[Int].toIO
        weightGramsHandler <- Handler.create[Int].toIO
        numberHandler <- Handler.create[Int].toIO
    } yield {

        val bestBeforeDateObservable = Observable
            .combineLatestMap(
                bestBeforeDayHandler.startWith(None.pure[List]),
                bestBeforeMonthHandler.startWith(None.pure[List]),
                bestBeforeYearHandler.startWith(None.pure[List])
            )((dayOpt, monthOpt, yearOpt) => yearOpt
                .map(year =>
                    BestBeforeDate(dayOpt, monthOpt, year)
                )
            )


        val supplies = Observable.combineLatestMap(
            nameHandler,
            bestBeforeDateObservable,
            kiloCaloriesHandler,
            weightGramsHandler,
            numberHandler
        )((name, bestBeforeDateOpt, kiloCalories, weightGrams, number) =>
            Supplies(randomId, name, bestBeforeDateOpt, kiloCalories, weightGrams, number)
        )
        .map[Option[Supplies]](Some.apply)
        .startWith(None.pure[List])

        div(
            div(
                div(
                    cls := "form-group",
                    label("Name"),
                    input(onInput.value --> nameHandler)
                ),
                div(
                    cls := "form-group",
                    label("Best Before Date"),
                    input("DD", typ := "number", inputAsOptionalInt --> bestBeforeDayHandler),
                    label("."),
                    input("MM", typ := "number", inputAsOptionalInt --> bestBeforeMonthHandler),
                    label("."),
                    input("YYYY", typ := "number", inputAsOptionalInt --> bestBeforeYearHandler)
                ),
                div(
                    cls := "form-group",
                    label("Kcal / 100g"),
                    input(typ := "number", inputAsInt --> kiloCaloriesHandler)
                ),
                div(
                    cls := "form-group",
                    label("Weight (g)"),
                    input(typ := "number", inputAsInt --> weightGramsHandler)
                ),
                div(
                    cls := "form-group",
                    label("Number"),
                    input(typ := "number", inputAsInt --> numberHandler)
                )
            ),
            div(supplies.map(_.toString))
        )
    }

    private val inputAsOptionalInt = onInput.value    
        .map(value => value.toIntOption)
        .map {
            case Some(0) => None
            case opt => opt
        }

    private val inputAsInt = onInput.valueAsNumber
        .filter(!_.isEmpty)
        .map(value => value.toInt)
        .filter(_ != 0)

    private def randomId = ju.UUID.randomUUID().toString()
}