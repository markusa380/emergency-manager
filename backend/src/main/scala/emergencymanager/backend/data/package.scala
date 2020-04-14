package emergencymanager.backend

import emergencymanager.commons.data.Id

import shapeless.record._
import shapeless.ops.record._

package object data {
    
    type Token = Record.`"userId" -> String, "expires" -> Long`.T
    type User = Record.`"passwordHash" -> Array[Byte], "salt" -> Array[Byte]`.T

    val tokenToIdTokenUpdater = Updater[Token, Id]
    type IdToken = tokenToIdTokenUpdater.Out

    val userToIdUserUpdater = Updater[User, Id]
    type IdUser = userToIdUserUpdater.Out
}
