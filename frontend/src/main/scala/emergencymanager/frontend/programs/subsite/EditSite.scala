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

import scala.concurrent.duration._

case class EditSite(
    dom: VNode,
    onExit: Observable[Unit],
    itemToEdit: Handler[String],
    connect: SyncIO[Unit]
)

object EditSite {

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
            .publish

        val validInput = Observable
            .combineLatestMap(
                itemObservable.map(_.id),
                Observable.merge(
                    nameInputHandler,
                    itemObservable.map(_.name)
                ),
                Observable.merge(
                    bbdInputHandler,
                    itemObservable.map(_.bestBefore.map(_.toString).getOrElse(""))
                ),
                Observable.merge(
                    kiloCaloriesInputHandler,
                    itemObservable.map(_.kiloCalories.toString)   
                ),
                Observable.merge(
                    weightInputHandler,
                    itemObservable.map(_.weightGrams.toString)
                ),
                Observable.merge(
                    numberInputHandler,
                    itemObservable.map(_.number.toString)
                )
            )(SuppliesValidator.validate(_)(_, _, _, _, _))

        val attemptOverwriteObservable = editHandler
            .debounce(100.millis)
            .withLatest(validInput)
            .map(_._2)

        val overwriteObservable = attemptOverwriteObservable
            .mapFilter(_.toOption)
            .concatMapAsync(Client.createItem)
            .publish

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

        val deleteObservable = deleteHandler
            .withLatest(itemHandler)
            .map(_._2)
            .concatMapAsync(Client.deleteItem)
            .publish

        val failedDeleteObservable = deleteObservable
            .failed
            .map("Delete failed: " + _.getMessage)

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
                                value := s.bestBefore.map(_.toString).getOrElse(""),
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
                        Observable.merge(
                            errorOverwriteObservable,
                            failedDeleteObservable
                        ).map(message =>
                            p(styles.color.red, message)
                        )
                    )
                )
            )
        )

        val onExit = Observable.merge(
            cancelHandler,
            overwriteObservable,
            deleteObservable
        )

        EditSite(
            dom,
            onExit,
            itemHandler,
            SyncIO {
                overwriteObservable.connect()
                deleteObservable.connect()
                // The two above need to consume the item from itemObservable so the order is important
                itemObservable.connect()
                println("Connected EditSite")
            }
        )
    }
}