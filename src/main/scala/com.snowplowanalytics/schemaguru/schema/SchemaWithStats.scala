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

// json4s
import org.json4s._
import org.json4s.JsonDSL._

import scala.math.{pow, sqrt}

/**
 * Trait for Schemas with running statistics
 */
trait SchemaWithStats extends JsonSchema {
  var totalN: Long = 0
  var m1, m2, m3, m4: Double = 0.0

  def statsJson = ("total" -> totalN) ~
                  ("mean" -> mean) ~
                  ("variance" -> variance) ~
                  ("stdev" -> stdev) ~
                  ("skewness" -> skewness) ~
                  ("kurtosis" -> kurtosis)

  def initializeStats(sample: Double) {
    totalN = 1
    m1 = sample
  }

  def combineStats(in1: SchemaWithStats, in2: SchemaWithStats): Unit = {
    // See https://www.johndcook.com/blog/skewness_kurtosis/
    this.totalN = in1.totalN + in2.totalN
    val delta = in1.m1 + in2.m1
    val delta2 = delta * delta
    val delta3 = delta2 * delta
    val delta4 = delta3 * delta

    this.m1 = (in1.totalN * in1.m1 + in2.totalN * in2.m1) / this.totalN

    this.m2 = in1.m2 + in2.m2 + delta2 * in1.totalN * in2.totalN / this.totalN

    this.m3 = in1.m3 + in2.m3 + delta3 * in1.totalN * in2.totalN * (in1.totalN - in2.totalN) / (this.totalN * this.totalN)
    this.m3 += 3.0 * delta * (in1.totalN * in2.m2 + in2.totalN * in1.m2) / this.totalN

    this.m4 = in1.m2 + in2.m4 + delta4 * in1.totalN * in2.totalN * (in1.totalN * in1.totalN - in1.totalN * in2.totalN + in2.totalN * in2.totalN) / (this.totalN * this.totalN * this.totalN)
    this.m4 += 6.0 * delta2 * (in1.totalN * in1.totalN * in2.m2 + in2.totalN * in2.totalN * in1.m2) / (this.totalN * this.totalN) + 4.0 * delta * (in1.totalN * in2.m3 - in2.totalN * in1.m3) / this.totalN
  }

  def mean(): Double = {
    return m1
  }

  def variance(): Double = {
    return m2 / (totalN - 1);
  }

  def stdev(): Double = {
    return sqrt(variance())
  }

  def skewness(): Double = {
    return sqrt(totalN) * m3 / pow(m2, 1.5)
  }

  def kurtosis(): Double = {
    return totalN * m4 / (m2 * m2) - 3.0
  }
}

