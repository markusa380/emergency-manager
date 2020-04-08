package emergencymanager.commons

import emergencymanager.commons.data._

import shapeless.labelled._
import shapeless.record._

package object ops {
    
    implicit class NewFoodItemOps(newFoodItem: NewFoodItem) {
        def withId(id: String): FoodItem = newFoodItem + field[FoodItems.IdKey](id)
    }

    implicit class FoodItemOps(foodItem: FoodItem) {
        def withoutId: NewFoodItem = foodItem - 'id
        def withUserId(userId: String): UserFoodItem = foodItem + field[FoodItems.UserIdKey](userId)
    }

    implicit class UserFoodItemOps(userFoodItem: UserFoodItem) {
        def withoutUserId: FoodItem = userFoodItem - 'userId
    }
}