package backend.model

case class Supplies(
    id: String,
    name: String,
    bestBefore: BestBeforeDate,
    kiloCalories: Int,
    weightGrams: Int
)

case class BestBeforeDate(
    day: Option[Int],    
    month: Option[Int],
    year: Int
)