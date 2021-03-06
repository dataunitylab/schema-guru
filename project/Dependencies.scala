/*
 * Copyright (c) 2014 Snowplow Analytics Ltd. All rights reserved.
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
import sbt._

object Dependencies {

  object V {
    // Java
    val yodaTime         = "2.10.10"
    val yodaConvert      = "2.2.1"
    val hyperLogLog      = "1.1"
    val jacksonDatabind  = "2.12.1"
    val jsonValidator    = "2.2.7"
    val commonsValidator = "1.7"
    // Scala
    val scopt            = "4.0.0"
    val scalaz7          = "7.0.8"
    val json4s           = "3.2.10"   // don't upgrade to 3.2.11 https://github.com/json4s/json4s/issues/212
    val jsonpath         = "0.6.10"
    val schemaddl        = "0.10.0"
    val akka             = "2.5.30"
    val spray            = "1.3.4"
    val spark            = "1.3.1"
    // Scala (test only)
    val specs2           = "2.3.13"
    val scalazSpecs2     = "0.2"
    val scalaCheck       = "1.12.6"
  }

  object Libraries {
    // Java
    val yodaTime         = "joda-time"                  %  "joda-time"                 % V.yodaTime
    val yodaConvert      = "org.joda"                   %  "joda-convert"              % V.yodaConvert
    val hyperLogLog      = "com.github.prasanthj"       % "hyperloglog"                % V.hyperLogLog
    val jacksonDatabind  = "com.fasterxml.jackson.core" %  "jackson-databind"          % V.jacksonDatabind
    val jsonValidator    = "com.github.java-json-tools" %  "json-schema-validator"     % V.jsonValidator
    val commonsValidator = "commons-validator"          %  "commons-validator"         % V.commonsValidator
    // Scala
    val scalaz7          = "org.scalaz"                 %% "scalaz-core"               % V.scalaz7
    val json4sJackson    = "org.json4s"                 %% "json4s-jackson"            % V.json4s
    val json4sScalaz     = "org.json4s"                 %% "json4s-scalaz"             % V.json4s
    val jsonpath         = "io.gatling"                 %% "jsonpath"                  % V.jsonpath
    val schemaddl        = "com.snowplowanalytics"      %% "schema-ddl"                % V.schemaddl
    val scopt            = "com.github.scopt"           %% "scopt"                     % V.scopt
    // Spray
    val akka             = "com.typesafe.akka"          %% "akka-actor"                % V.akka
    val sprayCan         = "io.spray"                   %% "spray-can"                 % V.spray
    val sprayRouting     = "io.spray"                   %% "spray-routing"             % V.spray
    // Spark
    val sparkCore        = "org.apache.spark"           %% "spark-core"                % V.spark
    // Scala (test only)
    val specs2           = "org.specs2"                 %% "specs2"                    % V.specs2         % Test
    val scalazSpecs2     = "org.typelevel"              %% "scalaz-specs2"             % V.scalazSpecs2   % Test
    val scalaCheck       = "org.scalacheck"             %% "scalacheck"                % V.scalaCheck     % Test
    val sprayTestkit     = "io.spray"                   %% "spray-testkit"             % V.spray          % Test
  }
}
