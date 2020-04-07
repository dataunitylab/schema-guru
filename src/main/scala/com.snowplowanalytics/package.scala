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
package com.snowplowanalytics

// Scala
import scala.collection.immutable.ListMap

// Scalaz
import scalaz._

// json4s
import org.json4s._

// This library
import schemaguru.Common.{ JsonFile, SchemaDescription }
import schemaguru.schema.JsonSchema

package object schemaguru {
  /**
   * Schema criterion restricted to revision: vendor/name/m-r-*
   * Tuple using as root key to bunch of Schemas differing only by addition
   * (vendor, name, model, revision)
   */
  // TODO: replace with iglu-core SchemaCriterion
  type RevisionCriterion = (String, String, Int, Int)

  type ModelCriterion = (String, String, Int)

  /**
   * List of Schema properties
   * First-level key is arbitrary property (like id, name etc)
   * Second-level is map of JSON Schema properties (type, enum etc)
   */
  type PropertyList = ListMap[String, Map[String, String]]

  /**
   * Result of JSON segmentation
   * First element contains list of errors
   * Second element is Map, where
   * key is Schema name found by JSON path and value is list of JSONs
   */
  type SegmentedJsons = (List[String], Map[String, List[JValue]])

  /**
   * Type alias for a Valid JSON Schema
   */
  type ValidSchema = Validation[String, JsonSchema]

  /**
   * Type alias for a valid JSON
   */
  type ValidJson = Validation[String, JValue]

  /**
   * Type Alias for a Valid list of JSONs
   */
  type ValidJsonList = List[Validation[String, JValue]]

  /**
   * Type Alias for a Valid list of JSON files
   */
  type ValidJsonFileList = List[Validation[String, JsonFile]]
}
