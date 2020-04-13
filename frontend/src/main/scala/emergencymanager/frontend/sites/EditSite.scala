package emergencymanager.frontend.sites

import emergencymanager.commons.data._
import emergencymanager.commons.implicits._
import emergencymanager.commons.Parser
import emergencymanager.commons.parser.FoodItemParser._

import emergencymanager.frontend._
import emergencymanager.frontend.Dom._

import cats.implicits._
import cats.effect._

import shapeless.record._
import shapeless.syntax.std.tuple.{hlistOps => _, _}

import outwatch._
import outwatch.dsl.{col => _, _}
import outwatch.reactive.handler._
import colibri._

import scala.concurrent.duration._

object EditSite {

    def create(itemId: IdField, exitObserver: Observer[Unit])(implicit client: Client[IO]): IO[VNode] = for {

        // ### Setup ### //

        item <- client.retrieveItem(itemId.id)

        // ### Handlers ### //

        nameInputHandler <- Handler.createF[IO, String](item("name"))
        bbdInputHandler <- Handler.createF[IO, String](item("bestBefore").map(_.mkString).getOrElse(""))
        kiloCaloriesInputHandler <- Handler.createF[IO, String](item("kiloCalories").toString)
        weightInputHandler <- Handler.createF[IO, String](item("weightGrams").toString)
        numberInputHandler <- Handler.createF[IO, String](item("number").toString)
        editHandler <- Handler.createF[IO, Unit]
        deleteHandler <- Handler.createF[IO, Unit]
        cancelHandler <- Handler.createF[IO, Unit]

        // ### Observables ### //

        validInput = Observable
            .combineLatest(
                nameInputHandler.map(Parser[FoodItem.Name, FoodItemMalformed].parse),
                bbdInputHandler.map(Parser[FoodItem.BestBefore, FoodItemMalformed].parse),
                kiloCaloriesInputHandler.map(Parser[FoodItem.KiloCalories, FoodItemMalformed].parse),
                weightInputHandler.map(Parser[FoodItem.Weight, FoodItemMalformed].parse),
                numberInputHandler.map(Parser[FoodItem.Number, FoodItemMalformed].parse)
            )
            .map(_.tupled.map[FoodItem.NewItem](_.productElements))
            .map(_.map(_.withId(itemId.id)))
            .doOnNext(v => println(s"Is valid: ${v.isValid}"))

        attemptOverwriteObservable = editHandler
            .doOnNext(_ => println(s"Attempting to save $itemId"))
            .debounce(100.millis)
            .withLatest(validInput)
            .map(_._2)

        overwriteObservable = attemptOverwriteObservable
            .mapFilter(_.toOption)
            .concatMapAsync(item => client.editItem(item).attempt)
            .publish

        successfulOverwriteObservable = overwriteObservable
            .mapFilter(_.toOption)
            .doOnNext(_ => println(s"Successfully saved item $itemId"))

        failedOverwriteObservable = overwriteObservable
            .mapFilter(_.left.toOption)
            .map(_.getMessage)
            .doOnNext(e => println(s"Failed to save $itemId: $e"))

        errorOverwriteObservable = Observable
            .merge(
                attemptOverwriteObservable
                    .mapFilter(_.toEither.left.toOption)
                    .map(_.map(_.message).reduce(_ + ". " + _)),
                failedOverwriteObservable
            )
            .doOnNext(t => println(s"Failed to save item $itemId: $t"))

        deleteObservable = deleteHandler
            .concatMapAsync(_ => client.deleteItem(itemId.id).attempt)
            .publish

        successfulDeleteObservable = deleteObservable
            .mapFilter(_.toOption)
            .doOnNext(_ => println(s"Deleted item $itemId"))

        failedDeleteObservable = deleteObservable
            .mapFilter(_.left.toOption)
            .map(_.getMessage)
            .doOnNext(m => println(s"Failed to delete item $itemId: $m"))

        onExit = Observable.merge(
            cancelHandler,
            successfulOverwriteObservable,
            successfulDeleteObservable
        )

        errorObservable = Observable.merge(
            errorOverwriteObservable,
            failedDeleteObservable
        )

        // ### DOM ### //

        dom = container(
            card(
                cardBody(
                    cardTitle("Edit Supplies"),
                    formGroup(
                        textInput(
                            placeholder := "Product Name",
                            formControlled,
                            value := item("name"),
                            onInput.value --> nameInputHandler
                        )
                    ),
                    formGroup(
                        textInput(
                            placeholder := "Best Before Date (DD.MM.YYYY)",
                            formControlled,
                            value := item("bestBefore").map(_.mkString).getOrElse(""),
                            onInput.value --> bbdInputHandler
                        )
                    ),
                    formGroup(
                        numberInput(
                            placeholder := "Kcal / 100g",
                            formControlled,
                            value := item("kiloCalories").toString,
                            onInput.value --> kiloCaloriesInputHandler
                        )
                    ),
                    formGroup(
                        numberInput(
                            placeholder := "Weight (g)",
                            formControlled,
                            value := item("weightGrams").toString,
                            onInput.value --> weightInputHandler
                        )
                    ),
                    formGroup(
                        numberInput(
                            placeholder := "Number",
                            formControlled,
                            value := item("number").toString,
                            onInput.value --> numberInputHandler
                        )
                    ),
                    formGroup(
                        cls := "button-group",
                        secondaryButton(
                            "Cancel",
                            onMouseDown.use(()) --> cancelHandler,
                            styles.marginRight := "5px"
                        ),
                        secondaryButton(
                            "Delete",
                            onMouseDown.use(()) --> deleteHandler,
                            styles.marginRight := "5px"
                        ),
                        primaryButton(
                            "Save",
                            onMouseDown.use(()) --> editHandler
                        )
                    ),
                    errorObservable
                        .map(message =>
                            p(styles.color.red, limitErrorMessageLength(message))
                        )
                )
            )
        )

        // ### Subscriptions ### //

        _ <- IO(onExit subscribe exitObserver)
        _ <- IO(deleteObservable.connect)
        _ <- IO(overwriteObservable.connect)

    } yield dom
}