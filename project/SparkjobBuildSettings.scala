/*
 * Copyright (c) 2015 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the
 * Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.  See the Apache License Version 2.0 for the specific
 * language governing permissions and limitations there under.
 */
import sbt._
import Keys._

object SparkjobBuildSettings {
  import BuildSettings._

  // Settings specific for Schema Guru web UI
  lazy val sparkjobSettings = Seq[Setting[_]](
    description           :=  "Spark job for Schema derivation",

    Compile / run / mainClass := Some("com.snowplowanalytics.schemaguru.sparkjob.Main")
  )

  import sbtassembly.AssemblyPlugin.autoImport._
  lazy val sbtAssemblySparkjobSettings = sbtAssemblyCommonSettings ++ Seq(
    // Drop these jars
    assembly / assemblyExcludedJars := {
      val cp = (fullClasspath in assembly).value
      cp filter { f =>
        f.data.getName == "commons-beanutils-1.8.3.jar"
      }
    },
    assembly / mainClass := Some("com.snowplowanalytics.schemaguru.sparkjob.Main")
  )

  lazy val sparkjobBuildSettings =
    commonSettings ++
    sparkjobSettings ++
    sbtAssemblySparkjobSettings
}
