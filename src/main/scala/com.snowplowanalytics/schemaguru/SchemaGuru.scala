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

// Scalaz
import scalaz._
import Scalaz._

// json4s
import org.json4s.JValue

// This library
import Common.{ JsonConvertResult, DerivedSchema, SchemaGuruResult }
import generators.{ SchemaGenerator, LevenshteinAnnotator }
import schema.JsonSchema
import schema.Helpers._

object SchemaGuru {
  /**
   * Takes the valid list of JSONs, converts them into micro-schemas (schemas
   * which will validate a single value)
   * Don't forget that inside ``jsonToSchema`` merge happening for
   *
   * @param jsonList The Validated JSON list
   * @param context cardinality for detecting possible enums
   * @return result result of converting instances to micro-schemas
   */
  def convertJsonsToSchema(jsonList: List[JValue], context: SchemaContext): JsonConvertResult = {

    val generator = SchemaGenerator(context)

    val schemaList: List[ValidSchema] =
      jsonList.map(generator.jsonToSchema)

    val validSchemas: List[JsonSchema] = schemaList.flatMap {
      case Success(json) => List(json)
      case _ => Nil
    }

    val failSchemas: List[String] = schemaList.flatMap {
      case Failure(str) => List(str)
      case _ => Nil
    }

    JsonConvertResult(validSchemas, failSchemas)
  }

  /**
   * Merge all micro-schemas into one, transform it, analyze for any warnings
   * like possible duplicated keys
   *
   * @param jsonConvertResult result of converting instances to micro-schemas
   * @param schemaContext context with all information for create and merge
   * @return result of merge and transformations with Schema, errors and warnings
   */
  def mergeAndTransform(jsonConvertResult: JsonConvertResult, schemaContext: SchemaContext): SchemaGuruResult = {

    implicit val monoid = getMonoid(schemaContext)

    val mergedSchema = jsonConvertResult.schemas.suml

    val schema = mergedSchema.transform { encaseNumericRange }
                             .transform { correctMaxLengths }
                             .transform { substituteEnums(schemaContext) }
                             .transform { flattenObjectProducts(schemaContext) }

    val duplicates = LevenshteinAnnotator.getDuplicates(extractKeys(schema))

    SchemaGuruResult(DerivedSchema(schema, None), jsonConvertResult.errors, Some(PossibleDuplicatesWarning(duplicates)))
  }
}
