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
import org.json4s.JsonDSL._

// This library
import Helpers._

/**
 * Schema for object values
 * http://spacetelescope.github.io/understanding-json-schema/reference/object.html
 *
 * @param properties map of keys to subschemas
 */
final case class ObjectSchema(properties: Map[String, JsonSchema], required: List[String] = List.empty[String], propertyCounts: Map[String, Int], totalCount: Int)(implicit val schemaContext: SchemaContext) extends JsonSchema {

  def toJson = {
    var json = ("type" -> "object") ~ ("properties" -> properties.map {
      case (key, value) => key -> value.toJson
    }) ~ ("propertyRatios" -> propertyCounts.map {
      case (key, value) => key -> value * 1.0 / totalCount
    }) ~ ("additionalProperties" -> false)
    if (schemaContext.requireFields) {
      json = json ~ ("required" -> required)
    }

    json
  }

  def mergeSameType(implicit schemaContext: SchemaContext) = {

    // Get monoid
    implicit val monoid = getMonoid(schemaContext)

    // Return partial function
    { case ObjectSchema(props, req, propCounts, totCount) => ObjectSchema(properties |+| props, required.intersect(req), propertyCounts |+| propCounts, totCount + totalCount) }
  }

  def getType = Set("object")

  override def transform(f: Function1[JsonSchema, JsonSchema]): JsonSchema = {
    val props = properties.map { case (k, v) => (k, f(v)) }
    this.copy(properties = props)
  }
}

