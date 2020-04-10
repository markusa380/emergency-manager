package emergencymanager.commons.instances

import emergencymanager.commons.ContraMapper

import shapeless._

trait ContraMapperInstances {
    
    implicit def hnilMapper[HF <: Poly]: ContraMapper.Aux[HF, HNil, HNil] = ContraMapper.instance(_ => HNil)

    implicit def hconsMapper[HF <: Poly, InH, InT <: HList, OutH, OutT <: HList](implicit
        hc : poly.Case1.Aux[HF, InH, OutH],
        mt : ContraMapper.Aux[HF, OutT, InT]
    ): ContraMapper.Aux[HF, OutH :: OutT, InH :: InT] = ContraMapper.instance(l => hc(l.head) :: mt(l.tail))
}