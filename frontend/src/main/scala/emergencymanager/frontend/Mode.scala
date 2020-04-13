package emergencymanager.frontend

import emergencymanager.commons.data.IdField

sealed trait Mode

case object LoginMode extends Mode

case object OverviewMode extends Mode

case class EditMode(id: IdField) extends Mode

case object CreateMode extends Mode