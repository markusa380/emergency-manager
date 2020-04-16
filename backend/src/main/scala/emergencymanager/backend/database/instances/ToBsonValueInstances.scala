package emergencymanager.backend.database.instances

import emergencymanager.backend.database._

import org.bson._
import org.bson.types.Decimal128

import scala.jdk.CollectionConverters._

trait ToBsonValueInstances {

    implicit val stringToBsonValue: ToBsonValue[String] = new ToBsonValue[String] {
        def apply(a: String): BsonValue = new BsonString(a)
    }

    implicit val booleanToBsonValue: ToBsonValue[Boolean] = new ToBsonValue[Boolean] {
        def apply(a: Boolean): BsonValue = new BsonBoolean(a)
    }

    implicit val binaryToBsonValue: ToBsonValue[Array[Byte]] = new ToBsonValue[Array[Byte]] {
        def apply(a: Array[Byte]): BsonValue = new BsonBinary(a)
    }

    implicit val intToBsonValue: ToBsonValue[Int] = new ToBsonValue[Int] {
        def apply(a: Int): BsonValue = new BsonInt32(a)
    }

    implicit val longToBsonValue: ToBsonValue[Long] = new ToBsonValue[Long] {
        def apply(a: Long): BsonValue = new BsonInt64(a)
    }

    implicit val floatToBsonValue: ToBsonValue[Float] = new ToBsonValue[Float] {
        def apply(a: Float): BsonValue = new BsonDecimal128(new Decimal128(BigDecimal(a.toDouble).bigDecimal))
    }

    implicit val doubleToBsonValue: ToBsonValue[Double] = new ToBsonValue[Double] {
        def apply(a: Double): BsonValue = new BsonDecimal128(new Decimal128(BigDecimal(a).bigDecimal))
    }

    implicit val bigDecimalToBsonValue: ToBsonValue[BigDecimal] = new ToBsonValue[BigDecimal] {
        def apply(a: BigDecimal): BsonValue = new BsonDecimal128(new Decimal128(a.bigDecimal))
    }

    implicit def listToBsonValue[A](implicit aToBsonValue: ToBsonValue[A]): ToBsonValue[List[A]] = new ToBsonValue[List[A]] {
        def apply(a: List[A]): BsonValue = new BsonArray(a.map(aToBsonValue.apply).asJava)
    }

    implicit def documentToBsonValue[A](implicit toBsonDocument: ToBsonDocument[A]): ToBsonValue[A] = new ToBsonValue[A] {
        def apply(a: A): BsonValue = toBsonDocument.apply(a)
    }
}