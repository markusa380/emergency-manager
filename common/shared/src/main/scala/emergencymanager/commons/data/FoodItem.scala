package emergencymanager.commons.data

import shapeless.labelled._
import shapeless.ops.record._
import shapeless.record._

object FoodItem {

    type UserId = FieldType["userId", String]
    type Name = FieldType["name", String]
    type BestBefore = FieldType["bestBefore", Option[BestBeforeDate]]
    type KiloCalories = FieldType["kiloCalories", Int]
    type Weight = FieldType["weightGrams", Int]
    type Number = FieldType["number", Int]

    type NewItem = Record.`"name" -> String, "bestBefore" -> Option[BestBeforeDate], "kiloCalories" -> Int, "weightGrams" -> Int, "number" -> Int`.T

    val newItemToUserItemUpdater = Updater[NewItem, UserId]
    type UserItem = newItemToUserItemUpdater.Out

    val newItemToIdItemUpdater = Updater[NewItem, Id]
    type IdItem = newItemToIdItemUpdater.Out

    val userItemToIdUserItemUpdater = Updater[UserItem, Id]
    type IdUserItem = userItemToIdUserItemUpdater.Out

    /* Old record types */

    type OldId = FieldType["id", String]
    type SearchName = FieldType["searchName", String]

    val toOldIdItemUpdater = Updater[NewItem, OldId]
    type OldIdItem = toOldIdItemUpdater.Out

    val toOldUserItemUpdater = Updater[OldIdItem, UserId]
    type OldUserItem = toOldUserItemUpdater.Out

    val toOldSearchableUserItem = Updater[OldUserItem, SearchName]
    type OldSearchableUserItem = toOldSearchableUserItem.Out

}
