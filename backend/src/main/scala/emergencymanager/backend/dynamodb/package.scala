package emergencymanager.backend

package object dynamodb {

    type ParseResult[A] = Either[ParseFailure, A]
}
