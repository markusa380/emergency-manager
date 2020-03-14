package emergencymanager.commons.data

import emergencymanager.commons.data.BestBeforeDate

case class Supplies(
    id: String,
    name: String,
    bestBefore: Option[BestBeforeDate],
    kiloCalories: Int,
    weightGrams: Int,
    number: Int
)