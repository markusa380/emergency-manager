package emergencymanager.commons

import shapeless._
import shapeless.labelled._
import shapeless.ops.record._
import shapeless.record._

package object data {

    object FoodItems {
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

        // For testing purposes
        type NewFoodItem2 = Name :: BestBeforeDate :: KiloCalories :: Weight :: Number :: HNil
        // the[NewFoodItem =:= NewFoodItem2] // Does not yet work
    }

    type NewFoodItem = Record.`'name -> String, 'bestBefore -> Option[BestBeforeDate], 'kiloCalories -> Int, 'weightGrams -> Int, 'number -> Int`.T

    val newFoodItemIdUpdater = Updater[NewFoodItem, FoodItems.Id]
    type FoodItem = newFoodItemIdUpdater.Out

    val foodItemUserIdUpdater = Updater[FoodItem, FoodItems.UserId]
    type UserFoodItem = foodItemUserIdUpdater.Out

    
}
