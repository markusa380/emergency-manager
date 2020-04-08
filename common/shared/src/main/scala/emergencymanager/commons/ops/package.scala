package emergencymanager.commons

import emergencymanager.commons.data._

import shapeless.labelled._
import shapeless.record._

package object ops {
    
    implicit class NewItemOps(newFoodItem: FoodItem.NewItem) {
        def withId(id: String): FoodItem.IdItem= newFoodItem + field[FoodItem.IdKey](id)
    }

    implicit class IdItemOps(foodItem: FoodItem.IdItem) {
        def withoutId: FoodItem.NewItem = foodItem - 'id
        def withUserId(userId: String): FoodItem.UserItem = foodItem + field[FoodItem.UserIdKey](userId)
    }

    implicit class UserItemOps(userFoodItem: FoodItem.UserItem) {
        def withoutUserId: FoodItem.IdItem= userFoodItem - 'userId
    }
}