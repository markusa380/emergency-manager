package emergencymanager.commons.data

case class Supplies(
    id: String,
    name: String,
    bestBefore: Option[BestBeforeDate],
    kiloCalories: Int,
    weightGrams: Int,
    number: Int
)

case class BestBeforeDate(
    day: Option[Int],    
    month: Option[Int],
    year: Int
)