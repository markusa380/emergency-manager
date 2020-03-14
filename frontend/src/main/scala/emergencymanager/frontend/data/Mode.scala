package emergencymanager.frontend.data

import outwatch._
import outwatch.dsl._

sealed trait Mode

case object LoginMode extends Mode

case object OverviewMode extends Mode

case class EditMode(id: String) extends Mode

case object CreateMode extends Mode