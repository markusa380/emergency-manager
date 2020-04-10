package emergencymanager.commons.ops

import emergencymanager.commons.data.FoodItem

import shapeless.labelled._
import shapeless.record._

trait FoodItemOps {
    
    implicit class NewItemOps(newFoodItem: FoodItem.NewItem) {
        def withId(id: String): FoodItem.IdItem= newFoodItem + field["id"](id)
    }

    implicit class IdItemOps(foodItem: FoodItem.IdItem) {
        def withoutId: FoodItem.NewItem = foodItem - "id"
        def withUserId(userId: String): FoodItem.UserItem = foodItem + field["userId"](userId)
    }

    implicit class UserItemOps(userFoodItem: FoodItem.UserItem) {
        def withoutUserId: FoodItem.IdItem= userFoodItem - "userId"
    }
}