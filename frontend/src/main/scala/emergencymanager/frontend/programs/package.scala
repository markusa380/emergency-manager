package emergencymanager.frontend

import outwatch._
import outwatch.dsl._

package object programs {
    
    val textInput = `type` := "text"
    val numericInput = `type` := "number"
    val passwordInput = `type` := "password"

    val onInputTextOption = onInput.value
        .map(raw =>
            if (raw.isEmpty) None
            else Some(raw)
        )

    val onInputOptionNumber = onInput.value
        .map(raw =>
            if (raw.isEmpty) None
            else raw.toIntOption
        )
}
