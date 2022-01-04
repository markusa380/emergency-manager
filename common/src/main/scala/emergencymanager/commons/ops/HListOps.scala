package emergencymanager.commons.ops

import emergencymanager.commons.ContraMapper

import shapeless._
import shapeless.labelled._

trait HListOps {

    private object PolyToField extends Poly1 {
        implicit def mapA[A, K]: Case.Aux[A, FieldType[K, A]] = at[A](a => field[K][A](a))
    }

    implicit final class HListOps[L <: HList](l : L) extends Serializable {

        def mapWithReturnType[Out <: HList](f: Poly)(implicit 
            mapper: ContraMapper.Aux[f.type, Out, L]
        ): Out = mapper(l)

        def mapToRecord[Out <: HList](implicit 
            mapper: ContraMapper.Aux[PolyToField.type, Out, L]
        ): Out = mapWithReturnType(PolyToField)
    }
}