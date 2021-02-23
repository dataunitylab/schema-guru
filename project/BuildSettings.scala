/*
 * Copyright (c) 2016 Snowplow Analytics Ltd. All rights reserved.
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

object BuildSettings {
  // Common settings for all our projects
  lazy val commonSettings = Seq[Setting[_]](
    organization          :=  "com.snowplowanalytics",
    version               :=  "0.6.2",
    scalaVersion          :=  "2.11.12",
    crossScalaVersions    :=  Seq("2.11.12"),
    scalacOptions         :=  Seq("-deprecation", "-encoding", "utf8",
                                  "-unchecked", "-feature",
                                  "-Xfatal-warnings", "-target:jvm-1.7"),
    Test / scalacOptions  :=  Seq("-Yrangepos")
  )

  // Settings specific for Schema Guru CLI
  lazy val coreSettings = Seq[Setting[_]](
    description           :=  "For deriving JSON Schemas from collections of JSON instances",

    Compile / run / mainClass := Some("com.snowplowanalytics.schemaguru.Main")
  )

  // sbt-assembly settings for building a fat jar
  import sbtassembly.AssemblyPlugin.autoImport._
  import sbtassembly.AssemblyPlugin.defaultShellScript
  lazy val sbtAssemblyCommonSettings = Seq(
    // Executable jarfile
    assembly / assemblyOption ~= { _.copy(prependShellScript = Some(defaultShellScript)) },

    // Name it as an executable
    assembly / assemblyJarName := { s"${name.value}-${version.value}" }
  )

  lazy val sbtAssemblyCoreSettings = sbtAssemblyCommonSettings ++ Seq(
    // Drop these jars
    assembly / assemblyExcludedJars := {
      val cp = (fullClasspath in assembly).value
      cp filter { f =>
        f.data.getName == "commons-beanutils-1.8.3.jar"
      }
    },
    assembly / mainClass := Some("com.snowplowanalytics.schemaguru.Main")
  )

  lazy val coreBuildSettings =
    commonSettings ++
    coreSettings ++
    sbtAssemblyCoreSettings
}
