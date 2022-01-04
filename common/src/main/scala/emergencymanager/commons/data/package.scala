package emergencymanager.commons

import shapeless.record._
import shapeless.labelled._

package object data {

    type Id = FieldType["_id", String]

    type Auth = Record.`"username" -> String, "password" -> String`.T

    type BestBeforeDate = Record.`"day" -> Option[Int], "month" -> Option[Int], "year" -> Int`.T

    type NameSearch = Record.`"name" -> String`.T
}
