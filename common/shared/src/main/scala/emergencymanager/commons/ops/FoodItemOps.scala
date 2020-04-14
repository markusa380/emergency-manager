package emergencymanager.commons.ops

import emergencymanager.commons.data._

import shapeless.labelled._
import shapeless.record._

trait FoodItemOps {
    
    implicit class NewItemOps(item: FoodItem.NewItem) {
        def withId(id: String): FoodItem.IdItem = item + field["_id"](id)
        def withUserId(userId: String): FoodItem.UserItem = item + field["userId"](userId)
    }

    @deprecated // TODO: Remove
    implicit class IdItemOps(item: FoodItem.OldIdItem) {
        def withoutId: FoodItem.NewItem = item - "id"
        def withUserId(userId: String): FoodItem.OldUserItem = item + field["userId"](userId)
    }

    @deprecated // TODO: Remove
    implicit class OldUserItemOps(item: FoodItem.OldUserItem) {
        def withoutUserId: FoodItem.OldIdItem = item - "userId"
        def withSearchName(searchName: String): FoodItem.OldSearchableUserItem = item + field["searchName"](searchName)

        def toUserItemV2: FoodItem.UserItem = item.withoutUserId.withoutId + field["userId"](item("userId"))
    }

    @deprecated // TODO: Remove
    implicit class OldSearchItemOps(item: FoodItem.OldSearchableUserItem) {
        def withoutSearchName: FoodItem.OldUserItem = item - "searchName"
    }
}