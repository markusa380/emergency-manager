package emergencymanager.frontend

import outwatch.dsl._

object Dom {

    val formControlled = cls := "form-control"
    val card = div(cls := "card")
    val cardBody = div(cls := "card-body")
    val cardTitle = h5(cls := "card-title")
    val container = div(cls := "container")
    val formGroup = div(cls := "form-group")
    val primaryButton = button(typ := "button", cls := "btn btn-primary")
    val secondaryButton = button(typ := "button", cls := "btn btn-secondary")
    val passwordInput = input(typ := "password")
    val textInput = input(typ := "text")
    val numberInput = input(typ := "number")
    val row = div(cls := "row")
    val col = div(cls := "col")
    def col(i: Int) = div(cls := s"col-$i")
}