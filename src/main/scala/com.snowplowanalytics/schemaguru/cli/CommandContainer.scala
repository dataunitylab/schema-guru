/*
 * Copyright (c) 2012-2016 Snowplow Analytics Ltd. All rights reserved.
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
package cli

// Java
import java.io.File

// This library
import Common.SchemaVer

/**
 * Common sealed traits for supported Schema Guru commands
 */
trait SchemaGuruCommand

/**
 * Command container was created due unability of scopt create
 * subcommands with different case classes configurations
 * It also contain shorthand-methods
 *
 * @param command one of required subcommands
 */
case class CommandContainer(command: Option[SchemaGuruCommand] = None) {

  // Common
  def setInput(input: File): Option[SchemaGuruCommand] =
    command match {
      case Some(schema: SchemaCommand) => Some(schema.copy(input = input))
      case other => other
    }

  def setOutput(output: File): CommandContainer =
    command match {
      case Some(command: SchemaCommand) => this.copy(command = Some(command.copy(output = Some(output))))
      case other                        => this
    }

  // schema
  def setEnumCardinality(cardinality: Int): Option[SchemaGuruCommand] =
    command match {
      case Some(schema: SchemaCommand) => Some(schema.copy(enumCardinality = cardinality))
      case other => other
    }

  def setEnumSets(sets: Seq[String]): Option[SchemaGuruCommand] =
    command match {
      case Some(schema: SchemaCommand) => Some(schema.copy(enumSets = sets))
      case other => other
    }

  def setVendor(vendor: String): Option[SchemaGuruCommand] =
    command match {
      case Some(schema: SchemaCommand) => Some(schema.copy(vendor = Some(vendor)))
      case other => other
    }

  def setName(name: String): Option[SchemaGuruCommand] =
    command match {
      case Some(schema: SchemaCommand) => Some(schema.copy(name = Some(name)))
      case other => other
    }

  def setSchemaver(schemaver: SchemaVer): Option[SchemaGuruCommand] =
    command match {
      case Some(schema: SchemaCommand) => Some(schema.copy(schemaver = Some(schemaver)))
      case other => other
    }

  def setSchemaBy(schemaBy: String): Option[SchemaGuruCommand] =
    command match {
      case Some(schema: SchemaCommand) => Some(schema.copy(schemaBy = Some(schemaBy)))
      case other => other
    }

  def setNoLength(flag: Boolean): Option[SchemaGuruCommand] =
    command match {
      case Some(schema: SchemaCommand) => Some(schema.copy(noLength = flag))
      case other => other
    }

  def setNdjson(flag: Boolean): Option[SchemaGuruCommand] =
    command match {
      case Some(schema: SchemaCommand) => Some(schema.copy(ndjson = flag))
      case other => other
    }
}
