package emergencymanager.backend

package object database {
  
    type ParseResult[A] = Either[ParseFailure, A]
}
