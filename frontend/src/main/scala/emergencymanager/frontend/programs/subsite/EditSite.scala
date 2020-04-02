package emergencymanager.frontend.programs.subsite

import emergencymanager.frontend.SuppliesValidator
import emergencymanager.frontend.Dom._
import emergencymanager.frontend.Client

import cats.implicits._
import cats.effect._

import outwatch._
import outwatch.dsl.{col => _, _}
import outwatch.reactive.handler._
import colibri._

import scala.concurrent.duration._


object EditSite {

    def create(itemId: String, exitObserver: Observer[Unit])(implicit ctx: ContextShift[IO]): IO[VNode] = for {

        // ### Setup ### //

        item <- Client.retrieveItem(itemId)

        // ### Handlers ### //

        nameInputHandler <- Handler.createF[IO, String](item.name)
        bbdInputHandler <- Handler.createF[IO, String](item.bestBefore.map(_.toString).getOrElse(""))
        kiloCaloriesInputHandler <- Handler.createF[IO, String](item.kiloCalories.toString)
        weightInputHandler <- Handler.createF[IO, String](item.weightGrams.toString)
        numberInputHandler <- Handler.createF[IO, String](item.number.toString)
        editHandler <- Handler.createF[IO, Unit]
        deleteHandler <- Handler.createF[IO, Unit]
        cancelHandler <- Handler.createF[IO, Unit]

        // ### Observables ### //

        validInput = Observable
            .combineLatestMap(
                nameInputHandler,
                bbdInputHandler,
                kiloCaloriesInputHandler,
                weightInputHandler,
                numberInputHandler,
            )(SuppliesValidator.validate(item.id))
            .doOnNext(v => println(s"Is valid: ${v.isValid}"))

        attemptOverwriteObservable = editHandler
            .doOnNext(_ => println(s"Attempting to save ${item.id}"))
            .debounce(100.millis)
            .withLatest(validInput)
            .map(_._2)

        overwriteObservable = attemptOverwriteObservable
            .mapFilter(_.toOption)
            .concatMapAsync(Client.createItem)
            .doOnNext(_ => println(s"Successfully saved ${item.id}"))
            .publish

        failedOverwriteObservable = overwriteObservable
            .failed
            .map(_.getMessage)

        errorOverwriteObservable = Observable
            .merge(
                attemptOverwriteObservable
                    .mapFilter(_.toEither.left.toOption)
                    .map(_.map(_.toString).reduce(_ + ", " + _))
                    .map("Validation failed: " + _),
                failedOverwriteObservable
                    .map("Edit failed: " + _)
            )
            .doOnNext(t => println(s"Failed to save item ${item.id}: $t"))

        deleteObservable = deleteHandler
            .concatMapAsync(_ => Client.deleteItem(item.id))
            .doOnNext(_ => println(s"Deleted item ${item.id}"))
            .publish

        failedDeleteObservable = deleteObservable
            .failed
            .map("Delete failed: " + _.getMessage)
            .doOnNext(m => println(s"Failed to delete item ${item.id}: $m"))

        onExit = Observable.merge(
            cancelHandler,
            overwriteObservable,
            deleteObservable
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
                            value := item.name,
                            onInput.value --> nameInputHandler
                        )
                    ),
                    formGroup(
                        textInput(
                            placeholder := "Best Before Date (DD.MM.YYYY)",
                            formControlled,
                            value := item.bestBefore.map(_.toString).getOrElse(""),
                            onInput.value --> bbdInputHandler
                        )
                    ),
                    formGroup(
                        numberInput(
                            placeholder := "Kcal / 100g",
                            formControlled,
                            value := item.kiloCalories.toString,
                            onInput.value --> kiloCaloriesInputHandler
                        )
                    ),
                    formGroup(
                        numberInput(
                            placeholder := "Weight (g)",
                            formControlled,
                            value := item.weightGrams.toString,
                            onInput.value --> weightInputHandler
                        )
                    ),
                    formGroup(
                        numberInput(
                            placeholder := "Number",
                            formControlled,
                            value := item.number.toString,
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
                    errorObservable.map(message =>
                        p(styles.color.red, message)
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