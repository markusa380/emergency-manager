package emergencymanager.commons

import shapeless.record._

package object data {

    type Auth = Record.`"username" -> String, "password" -> String`.T

    type BestBeforeDate = Record.`"day" -> Option[Int], "month" -> Option[Int], "year" -> Int`.T
}
