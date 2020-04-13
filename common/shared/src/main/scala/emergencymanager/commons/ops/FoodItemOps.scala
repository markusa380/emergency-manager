package emergencymanager.commons.ops

import emergencymanager.commons.data._

import shapeless.labelled._
import shapeless.record._

trait FoodItemOps {
    
    implicit class NewItemOps(item: FoodItem.NewItem) {
        def withId(id: String): FoodItem.IdItem = item + field["id"](id)
        def withUserId(userId: String): FoodItem.UserItem2 = item + field["userId"](userId)
    }

    implicit class IdItemOps(item: FoodItem.IdItem) {
        def withoutId: FoodItem.NewItem = item - "id"
        def withUserId(userId: String): FoodItem.UserItem = item + field["userId"](userId)
    }

    implicit class UserItemOps(item: FoodItem.UserItem) {
        def withoutUserId: FoodItem.IdItem = item - "userId"
        def withSearchName(searchName: String): FoodItem.SearchableUserItem = item + field["searchName"](searchName)

        def toUserItemV2: FoodItem.UserItem2 = item.withoutUserId.withoutId + field["userId"](item("userId"))
    }

    @deprecated
    implicit class SearchItemOps(item: FoodItem.SearchableUserItem) {
        def withoutSearchName: FoodItem.UserItem = item - "searchName"
    }
}