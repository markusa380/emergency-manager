package emergencymanager.frontend

sealed trait Mode

case object LoginMode extends Mode

case object OverviewMode extends Mode

case class EditMode(id: String) extends Mode

case object CreateMode extends Mode