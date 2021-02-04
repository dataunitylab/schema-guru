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

// json4s
import org.json4s.JsonDSL._

// This library
import Helpers.SchemaContext

final case class ObjectProductSchema(objects: List[ObjectSchema])(implicit val schemaContext: SchemaContext) extends JsonSchema with SchemaWithTransform[ObjectProductSchema] {
  def toJson = ("anyOf" -> objects.map(_.toJson))

  def mergeSameType(implicit schemaContext: SchemaContext) = {
    case ObjectProductSchema(otherObjects) => ObjectProductSchema((objects ++ otherObjects).distinct)
    case obj: ObjectSchema => if (objects.contains(obj)) {
      this
    } else {
      ObjectProductSchema(objects :+ obj)
    }
  }

  def getType = Set("object")

  def transform(f: PartialFunction[JsonSchema, JsonSchema]): ObjectProductSchema = {
    val objs = objects.map { o => o.transform(f) }
    this.copy(objects = objs)
  }
}
