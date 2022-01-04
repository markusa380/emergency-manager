package emergencymanager.commons.parser

import emergencymanager.commons.data._
import emergencymanager.commons.Parser

import emergencymanager.commons.implicits._

import cats.implicits._
import cats.data.ValidatedNec

import shapeless.labelled._
import shapeless.syntax.std.tuple._

object FoodItemParser {
        
    type ParseResult[A] = ValidatedNec[FoodItemMalformed, A]

    implicit val nameParser = new Parser[FoodItem.Name, FoodItemMalformed] {
        def parse(s: String): ParseResult[FoodItem.Name] =
            field["name"](s).validNec
    }

    implicit val bestBeforeDateParser = new Parser[FoodItem.BestBefore, FoodItemMalformed] {
        def parse(s: String): ParseResult[FoodItem.BestBefore] = {
            val parsed = s
                .trim
                .split('.')
                .toList match {
                    case "" +: Nil => None.validNec
                    case yr +: Nil => yr.toIntOption match {
                        case None => BestBeforeDateMalformed.invalidNec
                        case Some(y) => (None, None, y).some.validNec
                    }
                    case mr +: yr +: Nil => (mr.toIntOption, yr.toIntOption)
                        .mapN { case (m, y) =>
                            (None, m.some, y).some
                        }
                        .toValidNec(BestBeforeDateMalformed)
                    case dr +: mr +: yr +: Nil => (dr.toIntOption, mr.toIntOption, yr.toIntOption)
                        .mapN { case (d, m, y) =>
                            (d.some, m.some, y).some
                        }
                        .toValidNec(BestBeforeDateMalformed)
                    case _ => BestBeforeDateMalformed.invalidNec
                }
            
            val result = parsed.map { opt =>
                val recordOpt = opt.map(_.productElements.mapToRecord[BestBeforeDate])
                field["bestBefore"](recordOpt)
            }

            result
        }
    }

    implicit val kiloCaloriesParser = new Parser[FoodItem.KiloCalories, FoodItemMalformed] {
        def parse(s: String): ParseResult[FoodItem.KiloCalories] = s
            .trim
            .toIntOption
            .toValidNec(KiloCaloriesNotNumeric)
            .map(field["kiloCalories"].apply)
    }

    implicit val weightParser = new Parser[FoodItem.Weight, FoodItemMalformed] {
        def parse(s: String): ParseResult[FoodItem.Weight] = s
            .trim
            .toIntOption
            .toValidNec(WeightNotNumeric)
            .map(field["weightGrams"].apply)
    }

    implicit val numberParser = new Parser[FoodItem.Number, FoodItemMalformed] {
        def parse(s: String): ParseResult[FoodItem.Number] = s
            .trim
            .toIntOption
            .toValidNec(NumberNotNumeric)
            .map(field["number"].apply)
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
}