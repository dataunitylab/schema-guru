/*
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
package generators

// specs2
import org.specs2.Specification
import org.specs2.matcher.JsonMatchers

// json4s
import org.json4s._
import org.json4s.jackson.JsonMethods._

// This library
import schema.JsonSchema
import schema.SchemaWithHistogram
import schema.types._
import schema.Helpers._

class HistogramSpec extends Specification with JsonMatchers { def is = s2"""
  Check histogram tracking
    merge with available bins                   $mergeAvailBins
    merge and combine bins                      $mergeCombineBins
  """

  val ctx = SchemaContext(0)

  val int1Schema = IntegerSchema(None, None, None, List((1, 1)))(ctx)
  val int2Schema = IntegerSchema(None, None, None, List((2, 1)))(ctx)

  val fullIntSchema = IntegerSchema(None, None, None, (1 to 100).map(i => (i.toFloat, 1)).toList)(ctx)

  def mergeAvailBins = {
    implicit val monoid = getMonoid(ctx)
    val merged = int1Schema.mergeSameType(ctx)(int2Schema).asInstanceOf[SchemaWithHistogram]
    
    merged.bins mustEqual List((1, 1), (2, 1))
  }

  def mergeCombineBins = {
    implicit val monoid = getMonoid(ctx)
    val merged = fullIntSchema.mergeSameType(ctx)(fullIntSchema).asInstanceOf[SchemaWithHistogram]

    merged.bins mustEqual (1 to 100).map(i => (i.toFloat, 2)).toList
  }
}
