package emergencymanager.backend.algebra.serde

package object dynamodb {

    type ParseResult[A] = Either[ParseFailure, A]
}
