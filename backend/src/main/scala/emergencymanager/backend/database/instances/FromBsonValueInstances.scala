package emergencymanager.backend.database.instances

import emergencymanager.backend.database._

import cats.implicits._

import org.bson.BsonValue

import scala.jdk.CollectionConverters._

trait FromBsonValueInstances {

    implicit val boolFromBsonValue = new FromBsonValue[Boolean] {
        def apply(value: BsonValue): ParseResult[Boolean] =
            ( if (value.isBoolean) Some(value.asBoolean) else None )
                .toRight(ParseFailure(s"BsonValue does not contain a boolean: $value"))
                .map(_.getValue)
    }

    implicit val byteArrayFromBsonValue = new FromBsonValue[Array[Byte]] {
        def apply(value: BsonValue): ParseResult[Array[Byte]] =
            ( if (value.isBinary) Some(value.asBinary) else None )
                .toRight(ParseFailure(s"BsonValue does not contain binary data: $value"))
                .map(_.getData)
    }

    implicit val stringFromBsonValue = new FromBsonValue[String] {
        def apply(value: BsonValue): ParseResult[String] =
            ( if (value.isString) Some(value.asString) else None )
                .toRight(ParseFailure(s"BsonValue does not contain a string: $value"))
                .map(_.getValue)
    }

    implicit def arrayFromBsonValue[A](implicit
        fromBsonValueA: FromBsonValue[A]
    ) = new FromBsonValue[List[A]] {
        def apply(value: BsonValue): ParseResult[List[A]] =
            ( if (value.isArray) Some(value.asArray) else None )
                .toRight(ParseFailure(s"BsonValue does not contain array: $value"))
                .map(_.getValues.asScala.toList) // Convert to Scala List
                .flatMap( _
                    .traverse(fromBsonValueA.apply)
                )
    }

    implicit val intFromBsonValue = new FromBsonValue[Int] {
        def apply(value: BsonValue): ParseResult[Int] =
            ( if (value.isInt32) Some(value.asInt32) else None )
                .toRight(ParseFailure(s"BsonValue does not contain an int32: $value"))
                .map(_.getValue)
    }

    implicit val longFromBsonValue = new FromBsonValue[Long] {
        def apply(value: BsonValue): ParseResult[Long] =
            ( if (value.isInt64) Some(value.asInt64) else None )
                .toRight(ParseFailure(s"BsonValue does not contain an int64: $value"))
                .map(_.getValue)
    }

    implicit val floatFromBsonValue = new FromBsonValue[Float] {
        def apply(value: BsonValue): ParseResult[Float] =
            ( if (value.isDecimal128) Some(value.asDecimal128) else None )
                .toRight(ParseFailure(s"BsonValue does not contain a decimal128: $value"))
                .map(_.getValue.floatValue)
    }

    implicit val doubleFromBsonValue = new FromBsonValue[Double] {
        def apply(value: BsonValue): ParseResult[Double] =
            ( if (value.isDecimal128) Some(value.asDecimal128) else None )
                .toRight(ParseFailure(s"BsonValue does not contain a decimal128: $value"))
                .map(_.getValue.doubleValue)
    }

    implicit val bigDecimalFromBsonValue = new FromBsonValue[BigDecimal] {
        def apply(value: BsonValue): ParseResult[BigDecimal] =
            ( if (value.isDecimal128) Some(value.asDecimal128) else None )
                .toRight(ParseFailure(s"BsonValue does not contain a decimal128: $value"))
                .map(_.getValue.bigDecimalValue)
    }

    implicit def objectFromBsonValue[A](implicit
        fromDocument: FromBsonDocument[A]
    ): FromBsonValue[A] = new FromBsonValue[A] {
        def apply(value: BsonValue): ParseResult[A] = for {
            nested <- ( if (value.isDocument) Some(value.asDocument) else None )
                .toRight(ParseFailure(s"BsonValue does not contain a nested item: $value"))
            res <- fromDocument(nested)
        } yield res
    }
}