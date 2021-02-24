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

import scala.collection.mutable.{ListBuffer, Set}
import scala.math.{exp, floor, log, random}
import scala.util.Random

object SchemaWithSamples {
  val MaxSamples = 100
}


/**
 * Trait for Schemas which keep samples of values
 */
trait SchemaWithSamples[A] extends JsonSchema {
  var totalSamples = 0

  var samples: List[A] = List.empty[A]

  private var nextSample = SchemaWithSamples.MaxSamples + 1

  private var sampleW = exp(log(random) / SchemaWithSamples.MaxSamples)

  def addSample(value: A) = {
    // Only keep the first 100 characters of strings
    val sampleValue = if (value.isInstanceOf[String]) {
      val valueStr = value.asInstanceOf[String]
      if (valueStr.length > 100) {
        (valueStr.take(100) + "…").asInstanceOf[A]
      } else {
        value
      }
    } else {
      value
    }

    // Fill the reservoir with samples
    if (samples.length < SchemaWithSamples.MaxSamples) {
      samples = sampleValue :: samples
    }
    totalSamples += 1

    // Use Algorithm L to determine the next sample to take
    if (totalSamples <= nextSample) {
      val replaceIndex = floor(random * SchemaWithSamples.MaxSamples).toInt
      samples = samples.slice(0, replaceIndex) ++ List(sampleValue) ++ samples.drop(replaceIndex + 1)

      nextSample += floor(log(random) / log(1 - sampleW)).toInt + 1
      sampleW = exp(log(random) / SchemaWithSamples.MaxSamples)
    }
  }

  def mergeSamples(samplesA: List[A], countA: Int, samplesB: List[A], countB: Int): Unit = {
    // Track already samples values from each set
    val aIndexes = ListBuffer(Random.shuffle(1 to samplesA.length): _*)
    val bIndexes = ListBuffer(Random.shuffle(1 to samplesB.length): _*)

    val sampleRatio = countA * 1.0 / (countA + countB)
    var newSamples: ListBuffer[A] = ListBuffer.empty

    // Randomly sample elements proportional to their original frequency
    while (newSamples.length < SchemaWithSamples.MaxSamples && (aIndexes.length > 0 || bIndexes.length > 0)) {
      if (aIndexes.length > 0 && (bIndexes.length == 0 || random <= sampleRatio)) {
        newSamples.append(samplesA(aIndexes.remove(0) - 1))
      } else {
        newSamples.append(samplesB(bIndexes.remove(0) - 1))
      }
    }

    samples = newSamples.toList
  }
}

