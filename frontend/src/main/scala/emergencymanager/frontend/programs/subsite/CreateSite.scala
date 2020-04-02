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

object CreateSite {

    def create(exitObserver: Observer[Unit])(implicit ctx: ContextShift[IO]): IO[VNode] = for {

        // ### Handlers ### //

        nameInputHandler <- Handler.createF[IO, String]("")
        bbdInputHandler <- Handler.createF[IO, String]("")
        kiloCaloriesInputHandler <- Handler.createF[IO, String]("")
        weightInputHandler <- Handler.createF[IO, String]("")
        numberInputHandler <- Handler.createF[IO, String]("")
        createHandler <- Handler.createF[IO, Unit]
        cancelHandler <- Handler.createF[IO, Unit]

        // ### Observables ### //

        validInput = Observable
            .combineLatestMap(
                nameInputHandler,
                bbdInputHandler,
                kiloCaloriesInputHandler,
                weightInputHandler,
                numberInputHandler
            )(SuppliesValidator.validate("create"))

        attemptCreateObservable = createHandler
            .debounce(100.millis)
            .withLatest(validInput)
            .map(_._2)

        createObservable = attemptCreateObservable
            .mapFilter(_.toOption)
            .concatMapAsync(Client.createItem)
            .publish

        failedCreateObservable = createObservable
            .failed
            .map(_.getMessage)

        errorCreateObservable = Observable.merge(
            attemptCreateObservable
                .mapFilter(_.toEither.left.toOption)
                .map(_.map(_.toString).reduce(_ + ", " + _))
                .map("Validation failed: " + _),
            failedCreateObservable
                .map("Creation failed: " + _)
        )

        onExit = Observable.merge(cancelHandler, createObservable)

        // ### DOM ### //

        dom = createDom(nameInputHandler, bbdInputHandler, kiloCaloriesInputHandler, weightInputHandler, numberInputHandler, cancelHandler, createHandler, errorCreateObservable)

        // ### Subscriptions ###//

        _ <- IO(onExit.subscribe(exitObserver))
        _ <- IO(createObservable.connect)

    } yield dom

    
    private def createDom(
        nameInputHandler: Handler[String],
        bbdInputHandler: Handler[String],
        kiloCaloriesInputHandler: Handler[String],
        weightInputHandler: Handler[String],
        numberInputHandler: Handler[String],
        cancelHandler: Handler[Unit],
        createHandler: Handler[Unit],
        errorCreateObservable: Observable[String]
    ): VNode = container(
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
}