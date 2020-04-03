package emergencymanager.commons.data

import emergencymanager.commons.data.BestBeforeDate

case class FoodItem(
    id: String,
    name: String,
    bestBefore: Option[BestBeforeDate],
    kiloCalories: Int,
    weightGrams: Int,
    number: Int
)