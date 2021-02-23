/*
 * Copyright (c) 2015 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package com.snowplowanalytics.schemaguru
package schema
package types

// Scalaz
import scalaz._
import Scalaz._

// json4s
import org.json4s._
import org.json4s.JsonDSL._

// This library
import Helpers.SchemaContext

/**
 * Schema for integer values
 * http://spacetelescope.github.io/understanding-json-schema/reference/numeric.html#integer
 *
 * @param minimum minimum bound
 * @param maximum maximum bound
 * @param enum set of all acceptable values
 * @param bins histogram of values
 */
final case class IntegerSchema(
  minimum: Option[BigInt] = None,
  maximum: Option[BigInt] = None,
  enum: Option[List[JValue]] = Some(Nil),
  bins: List[(Float, Int)] = List.empty[(Float, Int)]
)(implicit val schemaContext: SchemaContext) extends JsonSchema with SchemaWithEnum with SchemaWithHistogram with SchemaWithHLL {

  def toJson = ("type" -> "integer") ~ ("maximum" -> maximum) ~ ("minimum" -> minimum) ~ ("enum" -> getJEnum) ~ ("distinctValues" -> hll.count)

  def mergeSameType(implicit schemaContext: SchemaContext) = {
    case other @ IntegerSchema(min, max, otherEnum, otherBins) => {
      val mergedEnums = mergeEnums(otherEnum)
      val mergedBins = mergeBins(otherBins)
      val newSchema = IntegerSchema(minOrNone(min, minimum), maxOrNone(max, maximum), mergedEnums, mergedBins)

      newSchema.hll.merge(hll)
      newSchema.hll.merge(other.hll)

      newSchema
    }
    case num: NumberSchema => num.merge(this)
  }

  def getType = Set("integer")

  override def getJEnum: JValue = enum
}

