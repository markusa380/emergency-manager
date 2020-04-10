package emergencymanager.commons.ops

import emergencymanager.commons.data.BestBeforeDate

import shapeless.record._

trait BestBeforeDateOps {

    implicit class BestBeforeDateOps(bbd: BestBeforeDate) {
        def mkString = (bbd("day").toList ++ bbd("month").toList ++ List(bbd("year")))
            .map(_.toString)
            .reduce(_ + '.' + _)
    }
}