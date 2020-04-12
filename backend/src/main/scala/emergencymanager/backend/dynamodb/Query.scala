package emergencymanager.backend.dynamodb

import shapeless.HList
import shapeless.ops.record._

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

trait Query[D] { self =>

    /**
      * Build the query
      *
      * @param index The index used for variable naming, necessary for composite queries
      * @return A tuple consisting of the expression, the expression attribute values and the expression attribute names
      */
    def build(index: Int): (Int, String, Map[String, AttributeValue], Map[String, String])

    /**
      * Build the query
      *
      * @return A tuple consisting of the expression and the expression attribute values
      */
    def build: (Int, String, Map[String, AttributeValue], Map[String, String]) = build(0)

    /**
      * Constructs a composite `and` query from this query and another query
      *
      * @param that The other query object
      * @return A composite `and` `Query` object
      */
    def and(that: Query[D]): Query[D] = new Query[D] {
        def build(index: Int): (Int, String, Map[String, AttributeValue], Map[String, String]) = {
            val thisBuilt = self.build(index)
            val thatBuilt = that.build(thisBuilt._1)

            (thatBuilt._1, thisBuilt._2 + " and " + thatBuilt._2, thisBuilt._3 ++ thatBuilt._3, thisBuilt._4 ++ thatBuilt._4)
        }
    }
}

object Query {
    
  def apply[D <: HList] = new QueryBuilder[D]

  class QueryBuilder[D <: HList]{
  
    /**
     * For safety we need some kind of evidence that a field in question
     * is actually a field in the given record D, with value of type String.
     */
    type IsStringField[K] = Selector.Aux[D, K, String]

    /**
      * Constructs a 'contains' - expression.
      *
      * @param value The value that the field should contain
      * @return A `Query` object that builds the 'contains' - expression
      */
    def contains[K <: String : ValueOf : IsStringField](value: String) = new Query[D] {
        def build(index: Int): (Int, String, Map[String, AttributeValue], Map[String, String]) = {
            
            val keyName = s"#key$index"
            val keyNameValue = valueOf[K]
            val varName = s":var$index"
            val keyValue = ToAttributeValue.to(value)

            (
              index + 1,
              s"contains ($keyName, $varName)",
              Map(varName -> keyValue),
              Map(keyName -> keyNameValue)
            )
        }
    }
  }
}