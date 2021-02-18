/* 
 * Copyright (c) 2015 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0, and
 * you may not use this file except in compliance with the Apache License
 * Version 2.0.  You may obtain a copy of the Apache License Version 2.0 at
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License Version 2.0 is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the Apache License Version 2.0 for the specific language
 * governing permissions and limitations there under.
 */
import sbt._
import Keys._

import Dependencies._
import BuildSettings._
import WebuiBuildSettings._
import SparkjobBuildSettings._

// Configure prompt to show current project.
shellPrompt := { s => Project.extract(s).currentProject.id + " > " }

lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "com.snowplowanalytics.schemaguru.generated",
    buildInfoObject := "ProjectSettings"
  )

// Define our project, with basic project information and library
// dependencies.
lazy val project = Project("schema-guru", file("."))
  .settings(coreBuildSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      // Java
      Libraries.yodaTime,
      Libraries.yodaConvert,
      Libraries.jacksonDatabind,
      Libraries.jsonValidator,
      Libraries.commonsValidator,
      // Scala
      Libraries.scalaz7,
      Libraries.json4sJackson,
      Libraries.json4sScalaz,
      Libraries.jsonpath,
      Libraries.schemaddl,
      Libraries.scopt,
      // Scala (test only)
      Libraries.specs2,
      Libraries.scalazSpecs2,
      Libraries.scalaCheck
    )
  )

lazy val webui = Project("schema-guru-webui", file("webui"))
  .settings(webuiBuildSettings: _*)
  .settings(compile in Compile := (compile in Compile).dependsOn(gulpDeployTask).value)
  .settings(
    libraryDependencies ++= Seq(
      // Scala
      Libraries.akka,
      Libraries.sprayCan,
      Libraries.sprayRouting,
      // Scala (test only)
      Libraries.specs2,
      Libraries.sprayTestkit
    )
  )
  .dependsOn(project)

lazy val sparkjob = Project("schema-guru-sparkjob", file("sparkjob"))
  .settings(sparkjobBuildSettings: _*)
  .settings(
    libraryDependencies ++= Seq(Libraries.sparkCore)
  )
  .dependsOn(project)
