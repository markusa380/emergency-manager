package emergencymanager.frontend.data

import emergencymanager.commons.data.Supplies

import outwatch._
import outwatch.dsl._

sealed trait Mode

case object LoginMode extends Mode

case object OverviewMode extends Mode

case object EditMode extends Mode

case object CreateMode extends Mode