package emergencymanager.commons.parser

import emergencymanager.commons.data._

import cats.implicits._
import cats.data.ValidatedNec

import shapeless.labelled._

object FoodItemParser {

    type ParseResult[A] = ValidatedNec[FoodItemMalformed, A]

    implicit val nameParser = new Parser[FoodItems.Name, FoodItemMalformed] {
        def parse(s: String): ParseResult[FoodItems.Name] =
            field[FoodItems.NameKey](s).validNec
    }

    implicit val bestBeforeDateParser = new Parser[FoodItems.BestBefore, FoodItemMalformed] {
        def parse(s: String): ParseResult[FoodItems.BestBefore] = {
            val parsed = s
                .trim
                .split('.')
                .toList match {
                    case "" :: Nil => None.validNec
                    case yr :: Nil => yr.toIntOption match {
                        case None => BestBeforeDateMalformed.invalidNec
                        case Some(y) => BestBeforeDate(None, None, y).some.validNec
                    }
                    case mr :: yr :: Nil => (mr.toIntOption, yr.toIntOption)
                        .mapN { case (m, y) => BestBeforeDate(None, m.some, y).some }
                        .toValidNec(BestBeforeDateMalformed)
                    case dr :: mr :: yr :: Nil => (dr.toIntOption, mr.toIntOption, yr.toIntOption)
                        .mapN { case (d, m, y) => BestBeforeDate(d.some, m.some, y).some }
                        .toValidNec(BestBeforeDateMalformed)
                    case _ => BestBeforeDateMalformed.invalidNec
                }
            
            parsed.map(field[FoodItems.BestBeforeKey].apply)
        }
    }



    implicit val kiloCaloriesParser = new Parser[FoodItems.KiloCalories, FoodItemMalformed] {
        def parse(s: String): ParseResult[FoodItems.KiloCalories] = s
            .trim
            .toIntOption
            .toValidNec(KiloCaloriesNotNumeric)
            .map(field[FoodItems.KiloCaloriesKey].apply)
    }

    implicit val weightParser = new Parser[FoodItems.Weight, FoodItemMalformed] {
        def parse(s: String): ParseResult[FoodItems.Weight] = s
            .trim
            .toIntOption
            .toValidNec(WeightNotNumeric)
            .map(field[FoodItems.WeightKey].apply)
    }

    implicit val numberParser = new Parser[FoodItems.Number, FoodItemMalformed] {
        def parse(s: String): ParseResult[FoodItems.Number] = s
            .trim
            .toIntOption
            .toValidNec(NumberNotNumeric)
            .map(field[FoodItems.NumberKey].apply)
    }
}

sealed trait FoodItemMalformed {
    def message: String
}

case object BestBeforeDateMalformed extends FoodItemMalformed {
    def message: String = "The best before date must be a date"
}

case object KiloCaloriesNotNumeric extends FoodItemMalformed {
    def message: String = "The kcal field must be numeric"
}

case object WeightNotNumeric extends FoodItemMalformed {
    def message: String = "The weight field must be numeric"
}

case object NumberNotNumeric extends FoodItemMalformed {
    def message: String = "The number field must be numeric"
}