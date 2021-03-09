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
package types

// Scalaz
import scalaz._
import Scalaz._

// json4s
import org.json4s._
import org.json4s.JsonDSL._

// This library
import Helpers.SchemaContext

/**
 * Product Schema is any schema that contain more than one type
 * Each contained type is represented by one field.
 * e.g. ["string", "number"], ["integer", "null"], ["object", "null"] etc
 *
 * @param objectSchema type information related to "object" type
 * @param arraySchema type information related to "array" type
 * @param stringSchema type information related to "string" type
 * @param integerSchema type information related to "integer" type
 * @param numberSchema type information related to "number" type
 * @param booleanSchema type information related to "boolean" type
 * @param nullSchema type information related to null
 */
final case class ProductSchema (
  objectSchema: Option[ObjectSchema] = None,
  objectSchemaCount: Int = 0,
  arraySchema: Option[ArraySchema] = None,
  arraySchemaCount: Int = 0,
  stringSchema: Option[StringSchema] = None,
  stringSchemaCount: Int = 0,
  integerSchema: Option[IntegerSchema] = None,
  numberSchema: Option[NumberSchema] = None,
  numberSchemaCount: Int = 0,
  booleanSchema: Option[BooleanSchema] = None,
  booleanSchemaCount: Int = 0,
  nullSchema: Option[NullSchema] = None,
  nullSchemaCount: Int = 0,
  totalCount: Int = 0
)(implicit val schemaContext: SchemaContext) extends JsonSchema {

  /**
   * List of all subtypes that this product schema really contains
   *
   * @return list of types
   */
  def types: List[JsonSchema] =
    List(objectSchema, arraySchema, stringSchema, integerSchema, numberSchema, booleanSchema, nullSchema).flatten

  def toJson =
    types
      .map(_.toJson)
      .foldLeft(JObject(Nil))(_.merge(_))  // this merge can break associativity
      // everything afterwards overrides previous values
      .merge(("type" -> getType): JObject)
      .transformField { case ("enum", _) => ("enum" -> getJEnum) }
      .asInstanceOf[JObject] ~
    ("typeRatios" -> ("object" -> objectSchemaCount * 1.0 / totalCount) ~
                     ("array" -> arraySchemaCount * 1.0 / totalCount) ~
                     ("string" -> stringSchemaCount * 1.0 / totalCount) ~
                     ("number" -> numberSchemaCount * 1.0 / totalCount) ~
                     ("boolean" -> booleanSchemaCount * 1.0 / totalCount) ~
                     ("null" -> nullSchemaCount * 1.0 / totalCount))

  def mergeSameType(implicit schemaContext: SchemaContext): PartialFunction[JsonSchema, ProductSchema] = {
    case ProductSchema(obj, objCount, arr, arrCount, str, strCount, int, num, numCount, bool, boolCount, nul, nulCount, totCount) => ProductSchema(
      mergeWithOption(obj, this.objectSchema).asInstanceOf[Option[ObjectSchema]],
      objCount + objectSchemaCount,
      mergeWithOption(arr, this.arraySchema).asInstanceOf[Option[ArraySchema]],
      arrCount + arraySchemaCount,
      mergeWithOption(str, this.stringSchema).asInstanceOf[Option[StringSchema]],
      strCount + stringSchemaCount,
      mergeInteger(int),
      mergeInteger(num, int),
      numCount + numberSchemaCount,
      mergeWithOption(bool,this.booleanSchema).asInstanceOf[Option[BooleanSchema]],
      boolCount + booleanSchemaCount,
      mergeWithOption(nul, this.nullSchema).asInstanceOf[Option[NullSchema]],
      nulCount + nullSchemaCount,
      totCount + totalCount
    )
  }

  override def merge(other: JsonSchema)(implicit schemaContext: SchemaContext): ProductSchema = other match {
    case prod: ProductSchema =>
      this.mergeSameType(schemaContext)(other)
    case obj: ObjectSchema =>
      this.copy(objectSchema = obj.merge(this.objectSchema).asInstanceOf[ObjectSchema].some, objectSchemaCount = objectSchemaCount + 1, totalCount = totalCount + 1)
    case arr: ArraySchema =>
      this.copy(arraySchema = arr.merge(this.arraySchema).asInstanceOf[ArraySchema].some, arraySchemaCount = arraySchemaCount + 1, totalCount = totalCount + 1)
    case str: StringSchema =>
      this.copy(stringSchema = str.merge(this.stringSchema).asInstanceOf[StringSchema].some, stringSchemaCount = stringSchemaCount + 1, totalCount = totalCount + 1)
    case int: IntegerSchema =>
      if (this.numberSchema.isDefined) // merge int to numberSchema's place and erase current integerSchema
        this.copy(numberSchema = int.merge(this.numberSchema).asInstanceOf[NumberSchema].some, integerSchema = None, numberSchemaCount = numberSchemaCount + 1, totalCount = totalCount + 1)
      else                             // merge int as usual
        this.copy(integerSchema = int.merge(this.integerSchema).asInstanceOf[IntegerSchema].some, numberSchemaCount = numberSchemaCount + 1, totalCount = totalCount + 1)
    case num: NumberSchema =>          // number and integer can't co-exist in same product type
      this.copy(integerSchema = None, numberSchema = num.merge(this.numberSchema).merge(this.integerSchema).asInstanceOf[NumberSchema].some, numberSchemaCount = numberSchemaCount + 1, totalCount = totalCount + 1)
    case bool: BooleanSchema =>
      this.copy(booleanSchema = bool.merge(this.booleanSchema).asInstanceOf[BooleanSchema].some, booleanSchemaCount = booleanSchemaCount + 1, totalCount = totalCount + 1)
    case nul: NullSchema =>
      this.copy(nullSchema = nul.merge(this.nullSchema).asInstanceOf[NullSchema].some, nullSchemaCount = nullSchemaCount + 1, totalCount = totalCount + 1)
    case zer: ZeroSchema =>
      this
  }

  def getType = types.map(_.getType).flatten.toSet

  override def transform(f: Function1[JsonSchema, JsonSchema]): JsonSchema =
    // XXX This assumes that transform will produce something of the same
    //     type which is currently true for these types but is not enforced
    this.copy(
      objectSchema.map(_.transform(f).asInstanceOf[ObjectSchema]),
      objectSchemaCount,
      arraySchema.map(_.transform(f).asInstanceOf[ArraySchema]),
      arraySchemaCount,
      stringSchema.map(_.transform(f).asInstanceOf[StringSchema]),
      stringSchemaCount,
      integerSchema.map(_.transform(f).asInstanceOf[IntegerSchema]),
      numberSchema.map(_.transform(f).asInstanceOf[NumberSchema]),
      numberSchemaCount,
      booleanSchema.map(_.transform(f).asInstanceOf[BooleanSchema]),
      booleanSchemaCount,
      nullSchema.map(_.transform(f).asInstanceOf[NullSchema]),
      nullSchemaCount,
      totalCount
    )

  override def getJEnum = types.map(_.getJEnum).reduceOption((a, b) => b.merge(a))

  /**
   * Merge two optional schemas or return exist schema if another is None
   *
   * @param a first schema type
   * @param b second schema type
   * @return merged if both present, none if both not present
   */
  def mergeWithOption(a: Option[JsonSchema], b: Option[JsonSchema]) = (a, b) match {
    case (Some(s), Some(o)) => Some(o.mergeSameType(schemaContext)(s))
    case (None, None)       => None
    case (Some(s), None)    => Some(s)
    case (None, Some(o))    => Some(o)
  }

  /**
   * Logic of merging integers and numbers in Product types
   * This function (and it's overloaded companion) will decide what schema type
   * should be returned on merging two product types
   *
   * @param int other's ``ProductType`` integer schema
   * @return
   */
  private def mergeInteger(int: Option[IntegerSchema]): Option[IntegerSchema] = {
    if (this.numberSchema.isDefined) { None }
    else { mergeWithOption(int, this.integerSchema).asInstanceOf[Option[IntegerSchema]] }
  }

  /**
   * Logic of merging integers and numbers in Product types
   * This function (and it's overloaded companion) will decide what schema type
   * should be returned on merging two product types
   *
   * @param num other's ``ProductType`` number schema
   * @param int other's ``ProductType`` integer schema
   * @return
   */
  private def mergeInteger(num: Option[NumberSchema], int: Option[IntegerSchema]): Option[NumberSchema] = {
    if (this.numberSchema.isDefined && num.isDefined) {
      val intMerged = mergeWithOption(int, this.numberSchema).asInstanceOf[Option[NumberSchema]]
      mergeWithOption(num, intMerged).asInstanceOf[Option[NumberSchema]]
    }
    else { mergeWithOption(num, this.numberSchema).asInstanceOf[Option[NumberSchema]] }
  }
}
