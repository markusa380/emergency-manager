package emergencymanager.frontend

import emergencymanager.commons.data.BestBeforeDate

import cats.data.ValidatedNec

import cats.implicits._

import emergencymanager.commons.data.FoodItem

sealed trait SuppliesValidation {
    def message: String
}

case object IdIsEmpty extends SuppliesValidation {
    def message: String = "The ID field cannot be empty"
}

case object NameIsEmpty extends SuppliesValidation {
    def message: String = "The name field cannot be empty"
}

case object BestBeforeDateInvalid extends SuppliesValidation {
    def message: String = "The best before date must be a valid date"
}

case object KiloCaloriesIsEmpty extends SuppliesValidation {
    def message: String = "The kcal field cannot be empty"
}

case object KiloCaloriesNotNumeric extends SuppliesValidation {
    def message: String = "The kcal field must be numeric"
}

case object KiloCaloriesLeqZero extends SuppliesValidation {
    def message: String = "The kcal field must be positive"
}

case object WeightIsEmpty extends SuppliesValidation {
    def message: String = "The weight field cannot be empty"
}

case object WeightNotNumeric extends SuppliesValidation {
    def message: String = "The weight field must be numeric"
}

case object WeightLeqZero extends SuppliesValidation {
    def message: String = "The weight field must be positive"
}

case object NumberNotNumeric extends SuppliesValidation {
    def message: String = "The number field must be numeric"
}

case object NumberIsEmpty extends SuppliesValidation {
    def message: String = "The number field cannot be empty"
}

case object NumberLeqZero extends SuppliesValidation {
    def message: String = "The number field must be positive"
}

object SuppliesValidator {

    type ValidatedSupplies[A] = ValidatedNec[SuppliesValidation, A]

    def validate(
        id: String
    )(
        name: String,
        bestBeforeDate: String,
        kiloCalories: String,
        weight: String,
        number: String
    ): ValidatedSupplies[FoodItem] =
        (
            // Do not validate the ID as it will never be an input field
            id.validNec,
            validateName(name),
            validateBestBeforeDate(bestBeforeDate),
            validateKiloCalories(kiloCalories),
            validateWeight(weight),
            validateNumber(number)
        ).mapN(FoodItem.apply)

    private def validateName(raw: String): ValidatedSupplies[String] =
        if(!raw.trim.isEmpty) raw.trim.validNec else NameIsEmpty.invalidNec

    private def validateBestBeforeDate(
        raw: String
    ): ValidatedSupplies[Option[BestBeforeDate]] = raw
        .split('.')
        .toList match {
            case "" :: Nil => None.validNec
            case yr :: Nil => yr.toIntOption match {
                case None => BestBeforeDateInvalid.invalidNec
                case Some(y) => BestBeforeDate(None, None, y).some.validNec
            }
            case mr :: yr :: Nil => (mr.toIntOption, yr.toIntOption)
                .mapN { case (m, y) => BestBeforeDate(None, m.some, y).some.validNec }
                .getOrElse(BestBeforeDateInvalid.invalidNec)
            case dr :: mr :: yr :: Nil => (dr.toIntOption, mr.toIntOption, yr.toIntOption)
                .mapN { case (d, m, y) => BestBeforeDate(d.some, m.some, y).some.validNec }
                .getOrElse(BestBeforeDateInvalid.invalidNec)
            case _ => BestBeforeDateInvalid.invalidNec
        }

    private def validateKiloCalories(raw: String) =
        validatePositiveInt(
            KiloCaloriesIsEmpty,
            KiloCaloriesNotNumeric,
            KiloCaloriesLeqZero
        )(raw)

    private def validateWeight(raw: String) =
        validatePositiveInt(
            WeightIsEmpty,
            WeightNotNumeric,
            WeightLeqZero
        )(raw)

    private def validateNumber(raw: String) =
        validatePositiveInt(
            NumberIsEmpty,
            NumberNotNumeric,
            NumberLeqZero
        )(raw)

    private def validatePositiveInt(
        emptyErr: SuppliesValidation,
        notNumericErr: SuppliesValidation,
        leqZeroErr: SuppliesValidation
    )(
        raw: String
    ): ValidatedSupplies[Int] = raw.trim match {
        case "" => emptyErr.invalidNec
        case raw => raw.toIntOption match {
            case None => notNumericErr.invalidNec
            case Some(parsed) => if(parsed <= 0) leqZeroErr.invalidNec else parsed.validNec
        }
    }
}