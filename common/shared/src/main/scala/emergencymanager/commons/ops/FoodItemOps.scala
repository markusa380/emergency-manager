package emergencymanager.commons.ops

import emergencymanager.commons.data.FoodItem

import shapeless.labelled._
import shapeless.record._

trait FoodItemOps {
    
    implicit class NewItemOps(item: FoodItem.NewItem) {
        def withId(id: String): FoodItem.IdItem= item + field["id"](id)
    }

    implicit class IdItemOps(item: FoodItem.IdItem) {
        def withoutId: FoodItem.NewItem = item - "id"
        def withUserId(userId: String): FoodItem.UserItem = item + field["userId"](userId)
    }

    implicit class UserItemOps(item: FoodItem.UserItem) {
        def withoutUserId: FoodItem.IdItem = item - "userId"
        def withSearchName(searchName: String): FoodItem.SearchableUserItem = item + field["searchName"](searchName)
    }

    implicit class SearchItemOps(item: FoodItem.SearchableUserItem) {
        def withoutSearchName: FoodItem.UserItem = item - "searchName"
    }
}