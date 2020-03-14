package emergencymanager.commons.data

case class BestBeforeDate(
    day: Option[Int],    
    month: Option[Int],
    year: Int
) {
    override def toString: String = (day.toList ++ month.toList ++ List(year))
        .map(_.toString)
        .reduce(_ + '.' + _)
}