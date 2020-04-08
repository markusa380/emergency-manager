package emergencymanager.commons.validation

import cats.data.ValidatedNec

trait Validation[A, E] {
    def validate(a: A): ValidatedNec[E, A]
}