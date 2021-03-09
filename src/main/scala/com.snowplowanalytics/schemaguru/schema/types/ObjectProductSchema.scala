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
import Helpers.SchemaContext

final case class ObjectProductSchema(objects: List[ObjectSchema])(implicit val schemaContext: SchemaContext) extends JsonSchema {
  def toJson = ("anyOf" -> objects.map(_.toJson))

  def mergeSameType(implicit schemaContext: SchemaContext) = {
    case ObjectProductSchema(otherObjects) =>
      if (schemaContext.mergeCommonFields) {
        otherObjects.foldLeft[JsonSchema](this)(_.merge(_))
      } else {
        ObjectProductSchema((objects ++ otherObjects).distinct)
      }

    case obj: ObjectSchema =>
      if (objects.contains(obj)) {
        this
      } else if (schemaContext.mergeCommonFields) {
        objects.find(_.properties.keySet.intersect(obj.properties.keySet).size > 0) match {
          // Merge common properties with this schema
          case Some(obj2) =>
            val newProps = (obj.properties.keySet ++ obj2.properties.keys).map { k =>
              val schema1 = obj.properties.getOrElse(k, ZeroSchema()).asInstanceOf[JsonSchema]
              val schema2 = obj2.properties.getOrElse(k, ZeroSchema()).asInstanceOf[JsonSchema]
              k -> schema1.merge(schema2)
            }.toMap

            val newPropCounts = obj.propertyCounts |+| obj2.propertyCounts
            val newRequired = obj.required.intersect(obj2.required)
            val newObj = new ObjectSchema(newProps, newRequired, newPropCounts, obj.totalCount + obj2.totalCount)

            ObjectProductSchema(objects.diff(List(obj2)) :+ newObj)

          // No common properties found, just add the separate schema
          case None => ObjectProductSchema(objects :+ obj)
        }
      } else {
        ObjectProductSchema(objects :+ obj)
      }
  }

  def getType = Set("object")

  override def transform(f: Function1[JsonSchema, JsonSchema]): JsonSchema = {
    // XXX This assumes ObjectSchema class does not change on transform
    val objs = objects.map(_.transform(f).asInstanceOf[ObjectSchema])
    this.copy(objects = objs)
  }
}
