package emergencymanager.commons.parser

import cats.data.ValidatedNec

trait Parser[A, E] {
    def parse(s: String): ValidatedNec[E, A]
}

object Parser {
    def apply[A, E](implicit p: Parser[A, E]) = p
}