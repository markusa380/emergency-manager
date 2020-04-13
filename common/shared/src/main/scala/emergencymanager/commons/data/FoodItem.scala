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
    type SearchName = FieldType["searchName", String]

    val newFoodItemIdUpdater = Updater[NewItem, Id]
    type IdItem = newFoodItemIdUpdater.Out

    val foodItemUserIdUpdater = Updater[IdItem, UserId]
    type UserItem = foodItemUserIdUpdater.Out

    val userItemSearchNameUpdater = Updater[UserItem, SearchName]
    type SearchableUserItem = userItemSearchNameUpdater.Out

    // NEW STUFF

    //                 NewItem
    //                /       \
    //             userId     _id
    //              /           \
    //         UserItem2      IdItem2
    //              \           /
    //              _id      userId
    //                 \      /
    //                IdUserItem2

    type BsonId = FieldType["_id", IdField]

    val foodItemUserIdUpdater2 = Updater[NewItem, UserId]
    type UserItem2 = foodItemUserIdUpdater2.Out

    val foodItemIdUpdater2 = Updater[NewItem, BsonId]
    type IdItem2 = foodItemIdUpdater2.Out

    val userItem2IdUpdater = Updater[UserItem2, BsonId]
    type IdUserItem2 = userItem2IdUpdater.Out

    val idItemUserIdUpdater = Updater[IdItem2, UserId]
    type UserIdItem2 = idItemUserIdUpdater.Out

}
