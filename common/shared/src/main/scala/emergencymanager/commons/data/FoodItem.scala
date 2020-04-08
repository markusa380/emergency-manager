package emergencymanager.commons.data

import shapeless._
import shapeless.labelled._
import shapeless.ops.record._
import shapeless.record._

object FoodItem {
    type NameKey = Witness.`'name`.T
    type Name = FieldType[NameKey, String]

    type BestBeforeKey = Witness.`'bestBefore`.T
    type BestBefore = FieldType[BestBeforeKey, Option[BestBeforeDate]]

    type KiloCaloriesKey = Witness.`'kiloCalories`.T
    type KiloCalories = FieldType[KiloCaloriesKey, Int]

    type WeightKey = Witness.`'weightGrams`.T
    type Weight = FieldType[WeightKey, Int]

    type NumberKey = Witness.`'number`.T
    type Number = FieldType[NumberKey, Int]

    type IdKey = Witness.`'id`.T
    type Id = FieldType[IdKey, String]

    type UserIdKey = Witness.`'userId`.T
    type UserId = FieldType[UserIdKey, String]

    type NewItem = Record.`'name -> String, 'bestBefore -> Option[BestBeforeDate], 'kiloCalories -> Int, 'weightGrams -> Int, 'number -> Int`.T

    val newFoodItemIdUpdater = Updater[NewItem, Id]
    type IdItem = newFoodItemIdUpdater.Out

    val foodItemUserIdUpdater = Updater[IdItem, UserId]
    type UserItem = foodItemUserIdUpdater.Out

    // For testing purposes
    type NewFoodItem2 = Name :: BestBeforeDate :: KiloCalories :: Weight :: Number :: HNil
    // the[FoodItem.NewItem=:= NewFoodItem2] // Does not yet work

}
