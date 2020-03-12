package emergencymanager.frontend.programs

import emergencymanager.commons.data.Supplies
import emergencymanager.commons.data.BestBeforeDate

import cats.implicits._
import cats.effect._

import outwatch._
import outwatch.dsl._

import outwatch.reactive.handler._
import colibri._

import scala.concurrent.duration._
import emergencymanager.frontend.Client

object Table {

    def create(implicit ctx: ContextShift[IO]): VNode = table(
        thead(
            tr(
                td("Product Name"),
                td("Best Before Day"),
                td("Best Before Month"),
                td("Best Before Year"),
                td("Kcal / 100g"),
                td("Weigth (g)"),
                td("#")
            )
        ),
        Client.loadSupplies.map(
            _.map(sup => createRow(sup))
        )
    )

    def createRow(supplies: Supplies): IO[VNode] = for {
        nameHandler             <- Handler.create[Option[String]].toIO
        bestBeforeDayHandler    <- Handler.create[Option[Int]].toIO
        bestBeforeMonthHandler  <- Handler.create[Option[Int]].toIO
        bestBeforeYearHandler   <- Handler.create[Option[Int]].toIO
        kiloCaloriesHandler     <- Handler.create[Option[Int]].toIO
        weightGramsHandler      <- Handler.create[Option[Int]].toIO
        numberHandler           <- Handler.create[Option[Int]].toIO
    } yield {

        val bestBeforeDateObservable = Observable
            .combineLatestMap(
                bestBeforeDayHandler,
                bestBeforeMonthHandler,
                bestBeforeYearHandler
            )((dayOpt, monthOpt, yearOpt) => yearOpt
                .map(year =>
                    BestBeforeDate(dayOpt, monthOpt, year)
                )
            )

        val suppliesObservable = Observable
            .combineLatestMap(
                nameHandler,
                bestBeforeDateObservable,
                kiloCaloriesHandler,
                weightGramsHandler,
                numberHandler
            )((nameOpt, bestBeforeDateOpt, kiloCaloriesOpt, weightGramsOpt, numberOpt) =>
                (nameOpt, kiloCaloriesOpt, weightGramsOpt, numberOpt).mapN {
                    case (name, kiloCalories, weightGrams, number) =>
                        Supplies(supplies.id, name, bestBeforeDateOpt, kiloCalories, weightGrams, number)
                }
            )

        tr(
            td(supplies.id),
            td(input(textInput,     onInputTextOption   --> nameHandler             )),
            td(input(numericInput,  onInputOptionNumber --> bestBeforeDayHandler    )),
            td(input(numericInput,  onInputOptionNumber --> bestBeforeMonthHandler  )),
            td(input(numericInput,  onInputOptionNumber --> bestBeforeYearHandler   )),
            td(input(numericInput,  onInputOptionNumber --> bestBeforeYearHandler   )),
            td(input(numericInput,  onInputOptionNumber --> kiloCaloriesHandler     )),
            td(input(numericInput,  onInputOptionNumber --> weightGramsHandler      )),
            td(input(numericInput,  onInputOptionNumber --> numberHandler           ))
        )
    }

    
}