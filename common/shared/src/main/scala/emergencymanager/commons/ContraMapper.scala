package emergencymanager.commons

import shapeless._

trait ContraMapper[HF, Out <: HList] extends Serializable {
  type In <: HList
  def apply(t: In): Out
}

object ContraMapper {

  type Aux[HF, Out <: HList, In0 <: HList] = 
    ContraMapper[HF, Out] { type In = In0 }

  def instance[HF, Out <: HList, In0 <: HList](f: In0 => Out): Aux[HF, Out, In0] = 
    new ContraMapper[HF, Out] {
      override type In = In0
      override def apply(t: In0): Out = f(t)
    }

}