package emergencymanager.commons.validation

import emergencymanager.commons.Validation
import emergencymanager.commons.data.FoodItem

import cats.data.ValidatedNec
import cats.implicits._

import shapeless._
import shapeless.labelled._
import shapeless.ops.hlist.Mapper

object FoodItemValidation {

    def validate[A <: HList](a: A)(implicit mapper: Mapper[ValidatePoly.type, A]) = a.map(ValidatePoly)

    private object ValidatePoly extends Poly1 {
        implicit def caseValidate[A](implicit v: Validation[A, FoodItemInvalid]) = at[A](v.validate)
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

    implicit val idValidation = new Validation[FoodItem.BsonId, FoodItemInvalid] {
        def validate(a: FoodItem.BsonId): ValidatedNec[FoodItemInvalid, FoodItem.BsonId] =
            a.validNec
    }

    implicit val nameValidation = new Validation[FoodItem.Name, FoodItemInvalid] {
        def validate(a: FoodItem.Name): cats.data.ValidatedNec[FoodItemInvalid, FoodItem.Name] =
            if(!a.trim.isEmpty) field["name"](a.trim).validNec else NameIsEmpty.invalidNec
    }

    implicit val bestBeforeDateValidation = new Validation[FoodItem.BestBefore, FoodItemInvalid] {
        def validate(a: FoodItem.BestBefore): ValidatedNec[FoodItemInvalid, FoodItem.BestBefore] =
            a.validNec // TODO: Check if it's a "possible" date
    }

    implicit val kiloCaloriesValidation = new Validation[FoodItem.KiloCalories, FoodItemInvalid] {
        def validate(a: FoodItem.KiloCalories): cats.data.ValidatedNec[FoodItemInvalid, FoodItem.KiloCalories] =
            if (a < 0) KiloCaloriesLeqZero.invalidNec else a.validNec
    }

    implicit val weightValidation = new Validation[FoodItem.Weight, FoodItemInvalid] {
        def validate(a: FoodItem.Weight): cats.data.ValidatedNec[FoodItemInvalid, FoodItem.Weight] =
            if (a < 0) WeightLeqZero.invalidNec else a.validNec
    }

    implicit val numberValidation = new Validation[FoodItem.Number, FoodItemInvalid] {
        def validate(a: FoodItem.Number): cats.data.ValidatedNec[FoodItemInvalid, FoodItem.Number] =
            if (a < 0) NumberLeqZero.invalidNec else a.validNec
    }
}