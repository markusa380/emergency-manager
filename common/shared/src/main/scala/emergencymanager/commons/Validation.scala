package emergencymanager.commons

import cats.data.ValidatedNec

trait Validation[A, E] {
    def validate(a: A): ValidatedNec[E, A]
}