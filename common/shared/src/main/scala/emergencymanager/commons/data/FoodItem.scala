package emergencymanager.commons.data

import shapeless.labelled._
import shapeless.ops.record._
import shapeless.record._

object FoodItem {

    type Name = FieldType["name", String]
    type BestBefore = FieldType["bestBefore", Option[BestBeforeDate]]
    type KiloCalories = FieldType["kiloCalories", Int]
    type Weight = FieldType["weightGrams", Int]
    type Number = FieldType["number", Int]

    type NewItem = Record.`"name" -> String, "bestBefore" -> Option[BestBeforeDate], "kiloCalories" -> Int, "weightGrams" -> Int, "number" -> Int`.T

    type Id = FieldType["id", String]
    type UserId = FieldType["userId", String]

    val newFoodItemIdUpdater = Updater[NewItem, Id]
    type IdItem = newFoodItemIdUpdater.Out

    val foodItemUserIdUpdater = Updater[IdItem, UserId]
    type UserItem = foodItemUserIdUpdater.Out
}
