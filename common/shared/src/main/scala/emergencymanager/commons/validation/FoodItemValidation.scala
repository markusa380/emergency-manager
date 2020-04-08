package emergencymanager.commons.validation

import emergencymanager.commons.data._

import cats.data.ValidatedNec
import cats.implicits._

import shapeless._
import shapeless.labelled._
import shapeless.ops.hlist.Mapper

object FoodItemValidation {

    implicit val idValidation = new Validation[FoodItems.Id, FoodItemInvalid] {
        def validate(a: FoodItems.Id): ValidatedNec[FoodItemInvalid, FoodItems.Id] =
            a.validNec
    }

    implicit val nameValidation = new Validation[FoodItems.Name, FoodItemInvalid] {
        def validate(a: FoodItems.Name): cats.data.ValidatedNec[FoodItemInvalid, FoodItems.Name] =
            if(!a.trim.isEmpty) field[FoodItems.NameKey](a.trim).validNec else NameIsEmpty.invalidNec
    }

    implicit val bestBeforeDateValidation = new Validation[FoodItems.BestBefore, FoodItemInvalid] {
        def validate(a: FoodItems.BestBefore): ValidatedNec[FoodItemInvalid, FoodItems.BestBefore] =
            a.validNec // TODO: Check if it's a "possible" date
    }

    implicit val kiloCaloriesValidation = new Validation[FoodItems.KiloCalories, FoodItemInvalid] {
        def validate(a: FoodItems.KiloCalories): cats.data.ValidatedNec[FoodItemInvalid, FoodItems.KiloCalories] =
            if (a < 0) KiloCaloriesLeqZero.invalidNec else a.validNec
    }

    implicit val weightValidation = new Validation[FoodItems.Weight, FoodItemInvalid] {
        def validate(a: FoodItems.Weight): cats.data.ValidatedNec[FoodItemInvalid, FoodItems.Weight] =
            if (a < 0) WeightLeqZero.invalidNec else a.validNec
    }

    implicit val numberValidation = new Validation[FoodItems.Number, FoodItemInvalid] {
        def validate(a: FoodItems.Number): cats.data.ValidatedNec[FoodItemInvalid, FoodItems.Number] =
            if (a < 0) NumberLeqZero.invalidNec else a.validNec
    }

    object validatePoly extends Poly1 {
        implicit def caseValidate[A](implicit v: Validation[A, FoodItemInvalid]) = at[A](v.validate)
    }

    def validate[F <: HList](foodItem: F)(implicit mapper: Mapper[validatePoly.type, F]) = foodItem.map(validatePoly)
}

sealed trait FoodItemInvalid {
    def message: String
}

case object IdIsEmpty extends FoodItemInvalid {
    def message: String = "The ID field cannot be empty"
}

case object NameIsEmpty extends FoodItemInvalid {
    def message: String = "The name field cannot be empty"
}

case object BestBeforeDateInvalid extends FoodItemInvalid {
    def message: String = "The best before date must be a valid date"
}

case object KiloCaloriesLeqZero extends FoodItemInvalid {
    def message: String = "The kcal field must be positive"
}

case object WeightLeqZero extends FoodItemInvalid {
    def message: String = "The weight field must be positive"
}

case object NumberLeqZero extends FoodItemInvalid {
    def message: String = "The number field must be positive"
}