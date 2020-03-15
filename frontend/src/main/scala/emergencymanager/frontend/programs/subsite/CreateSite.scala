package emergencymanager.frontend.programs.subsite

import emergencymanager.frontend.SuppliesValidator
import emergencymanager.frontend.Dom._
import emergencymanager.frontend.Client

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

case class CreateSite(
    dom: VNode,
    onExit: Observable[Unit],
    connect: SyncIO[Unit]
)

object CreateSite {

    def create(
        implicit ctx: ContextShift[IO]
    ): SyncIO[CreateSite] = for {
        nameInputHandler <- Handler.create[String]("")
        bbdInputHandler <- Handler.create[String]("")
        kiloCaloriesInputHandler <- Handler.create[String]("")
        weightInputHandler <- Handler.create[String]("")
        numberInputHandler <- Handler.create[String]("")
        createHandler <- Handler.create[Unit]
        cancelHandler <- Handler.create[Unit]
    } yield {

        val validInput = Observable
            .combineLatestMap(
                nameInputHandler,
                bbdInputHandler,
                kiloCaloriesInputHandler,
                weightInputHandler,
                numberInputHandler
            )(SuppliesValidator.validate("create"))

        val attemptCreateObservable = createHandler
            .debounce(100.millis)
            .withLatest(validInput)
            .map(_._2)

        val createObservable = attemptCreateObservable
            .mapFilter(_.toOption)
            .concatMapAsync(Client.createItem)
            .publish

        val failedCreateObservable = createObservable
            .failed
            .map(_.getMessage)

        val errorCreateObservable = Observable.merge(
            attemptCreateObservable
                .mapFilter(_.toEither.left.toOption)
                .map(_.map(_.toString).reduce(_ + ", " + _))
                .map("Validation failed: " + _),
            failedCreateObservable
                .map("Creation failed: " + _)
        )

        val dom = container(
            card(
                cardBody(
                    cardTitle("Create Supplies"),
                    formGroup(
                        textInput(
                            placeholder := "Product Name",
                            formControlled,
                            onInput.value --> nameInputHandler
                        )
                    ),
                    formGroup(
                        textInput(
                            placeholder := "Best Before Date (DD.MM.YYYY)",
                            formControlled,
                            onInput.value --> bbdInputHandler
                        )
                    ),
                    formGroup(
                        numberInput(
                            placeholder := "Kcal / 100g",
                            formControlled,
                            onInput.value --> kiloCaloriesInputHandler
                        )
                    ),
                    formGroup(
                        numberInput(
                            placeholder := "Weight (g)",
                            formControlled,
                            onInput.value --> weightInputHandler
                        )
                    ),
                    formGroup(
                        numberInput(
                            placeholder := "Number",
                            formControlled,
                            onInput.value --> numberInputHandler
                        )
                    ),
                    div(
                        cls := "button-group",
                        secondaryButton(
                            "Cancel",
                            onMouseDown.use(()) --> cancelHandler,
                            styles.marginRight := "5px"
                        ),
                        primaryButton(
                            "Create",
                            onMouseDown.use(()) --> createHandler
                        ),

                    ),
                    errorCreateObservable.map(message =>
                        p(styles.color.red, message)
                    )
                )
            )
        )

        val onExit = Observable.merge(
            cancelHandler,
            createObservable
        )
        
        CreateSite(
            dom,
            onExit,
            SyncIO {
                createObservable.connect()
                println("Connected CreateSite")
            }
        )
    }
}