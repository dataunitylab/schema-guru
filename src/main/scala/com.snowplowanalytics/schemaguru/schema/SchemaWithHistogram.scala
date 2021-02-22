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
package schema

object SchemaWithHistogram {
  val MaxBins = 100
}

/**
 * Trait for Schemas which can have histograms
 */
trait SchemaWithHistogram extends JsonSchema {
  val bins: List[(Float, Int)]

  def shrinkBins(otherBins: List[(Float, Int)]): List[(Float, Int)] = {
    var newBins = otherBins

    while (newBins.size > SchemaWithHistogram.MaxBins) {
      // Find the pair of bins with the smallest difference
      var minDiff = Float.MaxValue
      var minIdx = 0

      newBins.zipWithIndex.sliding(2).foreach { case Seq(((q1, _), i), ((q2, _), _)) =>
        val diff = q2 - q1
        if (diff < minDiff) {
          minDiff = diff
          minIdx = i
        }
      }

      val newCount = newBins(minIdx)._2 + newBins(minIdx + 1)._2
      val newBin = ((newBins(minIdx)._1 * newBins(minIdx)._2 + newBins(minIdx + 1)._1 * newBins(minIdx + 1)._2) / newCount, newCount)
      newBins = newBins.slice(0, minIdx) ++ List(newBin) ++ newBins.drop(minIdx + 2)
    }

    newBins
  }

  def mergeBins(other: List[(Float, Int)]): List[(Float, Int)] = {
    shrinkBins((bins ++ other).sorted)
  }
}
