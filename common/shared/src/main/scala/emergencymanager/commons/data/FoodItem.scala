package emergencymanager.commons.data

import emergencymanager.commons.data.BestBeforeDate

import shapeless._
import shapeless.syntax._
import shapeless.labelled._
import shapeless.ops.record._
import shapeless.tag._
import shapeless.record._

case class FoodItem(
    id: String,
    name: String,
    bestBefore: Option[BestBeforeDate],
    kiloCalories: Int,
    weightGrams: Int,
    number: Int
) {
    def withUserId(userId: String): FoodItem.WithUserId =
        FoodItem.labelledGeneric.to(this) +
        FoodItem.userIdBuilder(userId)
}

object FoodItem {
    
    val labelledGeneric = LabelledGeneric[FoodItem]

    type Repr = labelledGeneric.Repr

    type UserIdKey = Symbol with Tagged[Witness.`"userId"`.T]
    
    val userIdBuilder = field[UserIdKey]

    type UserIdField = FieldType[UserIdKey, String]

    val foodItemUserIdUpdater = Updater[FoodItem.Repr, UserIdField]

    type WithUserId = foodItemUserIdUpdater.Out

    def toFoodItem(f: WithUserId) = labelledGeneric.from(f - 'userId)
}