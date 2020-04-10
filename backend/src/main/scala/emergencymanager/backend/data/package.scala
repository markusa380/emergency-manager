package emergencymanager.backend

import shapeless.record._

package object data {
    type Token = Record.`"id" -> String, "userId" -> String, "expires" -> Long`.T
    type User = Record.`"id" -> String, "passwordHash" -> List[Byte], "salt" -> List[Byte]`.T
}
