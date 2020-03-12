package emergencymanager.backend.data

import emergencymanager.commons.data.BestBeforeDate

case class EMSupplies(
    id: String,
    name: String,
    userId: String,
    bestBefore: Option[BestBeforeDate],
    kiloCalories: Int,
    weightGrams: Int,
    number: Int
)