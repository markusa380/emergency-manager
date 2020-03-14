package emergencymanager.frontend

import outwatch._
import outwatch.dsl._

object Dom {

    val formControlled = cls := "form-control"
    val card = div(cls := "card")
    val cardBody = div(cls := "card-body")
    val cardTitle = h5(cls := "card-title")
    val container = div(cls := "container")
    val formGroup = div(cls := "form-group")
    val primaryButton = button(cls := "btn btn-primary")
    val passwordInput = input(typ := "password")
    val textInput = input(typ := "text")
}