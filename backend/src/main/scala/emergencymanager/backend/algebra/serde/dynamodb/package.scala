package emergencymanager.backend.algebra.serde

import java.io.IOException

package object dynamodb {

    case class ParseFailure(message: String) extends IOException(message)
    
    type ParseResult[A] = Either[ParseFailure, A]
}
