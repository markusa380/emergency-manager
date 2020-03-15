package emergencymanager.frontend

import emergencymanager.commons.data.BestBeforeDate

import cats.data.ValidatedNec

import cats.implicits._

import emergencymanager.commons.data.Supplies

sealed trait SuppliesValidation {}

case object IdIsEmpty extends SuppliesValidation {}

case object NameIsEmpty extends SuppliesValidation {}

case object BestBeforeDateInvalid extends SuppliesValidation {}

case object KiloCaloriesIsEmpty extends SuppliesValidation {}

case object KiloCaloriesNotNumeric extends SuppliesValidation {}

case object KiloCaloriesLeqZero extends SuppliesValidation {}

case object WeightIsEmpty extends SuppliesValidation {}

case object WeightNotNumeric extends SuppliesValidation {}

case object WeightLeqZero extends SuppliesValidation {}

case object NumberNotNumeric extends SuppliesValidation {}

case object NumberIsEmpty extends SuppliesValidation {}

case object NumberLeqZero extends SuppliesValidation {}

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
    ): ValidatedSupplies[Supplies] =
        (
            // Do not validate the ID as it will never be an input field
            id.validNec,
            validateName(name),
            validateBestBeforeDate(bestBeforeDate),
            validateKiloCalories(kiloCalories),
            validateWeight(weight),
            validateNumber(number)
        ).mapN(Supplies)

    def validateName(raw: String): ValidatedSupplies[String] =
        if(!raw.trim.isEmpty) raw.trim.validNec else NameIsEmpty.invalidNec

    def validateBestBeforeDate(
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
            case invalid => BestBeforeDateInvalid.invalidNec
        }

    def validateKiloCalories(raw: String) =
        validatePositiveInt(
            KiloCaloriesIsEmpty,
            KiloCaloriesNotNumeric,
            KiloCaloriesLeqZero
        )(raw)

    def validateWeight(raw: String) =
        validatePositiveInt(
            WeightIsEmpty,
            WeightNotNumeric,
            WeightLeqZero
        )(raw)

    def validateNumber(raw: String) =
        validatePositiveInt(
            NumberIsEmpty,
            NumberNotNumeric,
            NumberLeqZero
        )(raw)

    def validatePositiveInt(
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