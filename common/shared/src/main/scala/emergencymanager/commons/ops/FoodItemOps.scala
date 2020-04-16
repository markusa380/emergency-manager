package emergencymanager.commons.ops

import emergencymanager.commons.data._

import shapeless.labelled._
import shapeless.record._

trait FoodItemOps {
    
    implicit class NewItemOps(item: FoodItem.NewItem) {
        def withId(id: String): FoodItem.IdItem = item + field["_id"](id)
        def withUserId(userId: String): FoodItem.UserItem = item + field["userId"](userId)
    }
}