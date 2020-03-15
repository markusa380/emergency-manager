package emergencymanager.frontend.programs.subsite

import emergencymanager.frontend.SuppliesValidator
import emergencymanager.frontend.Dom._
import emergencymanager.frontend.Client

import emergencymanager.commons.data.Supplies

import cats.data.Validated
import cats.data.Validated._
import cats.syntax._
import cats.implicits._
import cats.effect._

import outwatch._
import outwatch.dsl.{col => _, _}
import outwatch.reactive.handler._
import colibri._

case class EditSite(
    dom: VNode,
    onExit: Observable[Unit],
    itemToEdit: Handler[String]
)

object EditSite {

    trait InputInvalid
    case object CannotBeEmpty extends InputInvalid
    case object MustBeNumeric extends InputInvalid

    type ValidatedInput[A] = Validated[InputInvalid, A]

    def create(
        implicit ctx: ContextShift[IO]
    ): SyncIO[EditSite] = for {
        itemHandler <- Handler.create[String]
        nameInputHandler <- Handler.create[String]
        bbdInputHandler <- Handler.create[String]
        kiloCaloriesInputHandler <- Handler.create[String]
        weightInputHandler <- Handler.create[String]
        numberInputHandler <- Handler.create[String]
        editHandler <- Handler.create[Unit]
        deleteHandler <- Handler.create[Unit]
        cancelHandler <- Handler.create[Unit]
    } yield {

        val itemObservable = itemHandler
            .concatMapAsync(Client.retrieveItem)

        val validInput = Observable
            .combineLatestMap(
                itemObservable.map(_.id),
                nameInputHandler
                    .withLatestMap(itemObservable)((_, s) => s.name),
                bbdInputHandler
                    .withLatestMap(itemObservable)((_, s) => s.bestBefore.map(_.toString).getOrElse("")),
                kiloCaloriesInputHandler
                    .withLatestMap(itemObservable)((_, s) => s.kiloCalories.toString),
                weightInputHandler
                    .withLatestMap(itemObservable)((_, s) => s.weightGrams.toString),
                numberInputHandler
                    .withLatestMap(itemObservable)((_, s) => s.number.toString)
            )(SuppliesValidator.validate(_)(_, _, _, _, _))

        val attemptOverwriteObservable = editHandler
            .withLatest(validInput)
            .map(_._2)

        val overwriteObservable = attemptOverwriteObservable
            .mapFilter(_.toOption)
            .concatMapAsync(Client.createItem)

        val failedOverwriteObservable = overwriteObservable
            .failed
            .map(_.getMessage)

        val errorOverwriteObservable = Observable.merge(
            attemptOverwriteObservable
                .mapFilter(_.toEither.left.toOption)
                .map(_.map(_.toString).reduce(_ + ", " + _))
                .map("Validation failed: " + _)
            ,
            failedOverwriteObservable
                .map("Edit failed: " + _)
        )

        val dom = container(
            card(
                itemObservable.map(s =>
                    cardBody(
                        cardTitle("Edit Supplies"),
                        formGroup(
                            textInput(
                                placeholder := "Product Name",
                                formControlled,
                                value := s.name,
                                onInput.value --> nameInputHandler
                            )
                        ),
                        formGroup(
                            textInput(
                                placeholder := "Best Before Date (DD.MM.YYYY)",
                                formControlled,
                                value := s.bestBefore.toString,
                                onInput.value --> bbdInputHandler
                            )
                        ),
                        formGroup(
                            numberInput(
                                placeholder := "Kcal / 100g",
                                formControlled,
                                value := s.kiloCalories.toString,
                                onInput.value --> kiloCaloriesInputHandler
                            )
                        ),
                        formGroup(
                            numberInput(
                                placeholder := "Weight (g)",
                                formControlled,
                                value := s.weightGrams.toString,
                                onInput.value --> weightInputHandler
                            )
                        ),
                        formGroup(
                            numberInput(
                                placeholder := "Number",
                                formControlled,
                                value := s.number.toString,
                                onInput.value --> numberInputHandler
                            )
                        )
                    )
                ),
                div(
                    cls := "button-group",
                    secondaryButton(
                        "Cancel",
                        onClick.use(()) --> cancelHandler,
                        styles.marginRight := "5px"
                    ),
                    primaryButton(
                        "Save",
                        onClick.use(()) --> editHandler
                    ),

                ),
                errorOverwriteObservable.map(message =>
                    p(styles.color.red, message)
                )
            )
        )

        val onExit = Observable.merge(
            cancelHandler,
            overwriteObservable
        )
        
        EditSite(dom, onExit, itemHandler)
    }
}