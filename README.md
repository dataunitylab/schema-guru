# Schema Guru

[ ![Build Status] [travis-image] ] [Travis]  [ ![Release] [release-image] ] [releases] [ ![License] [license-image] ] [license]

Schema Guru is a tool (CLI, Spark job and web) allowing you to derive **[JSON Schemas] [json-schema]** from a set of JSON instances process and transform it into different data definition formats.

Current primary features include:

- Derivation of JSON Schema from set of JSON instances (``schema`` command)

Unlike other tools for deriving JSON Schemas, Schema Guru allows you to derive schema from an unlimited set of instances (making schemas much more precise), and supports many more JSON Schema validation properties.

Schema Guru is used heavily in association with Snowplow's own **[Snowplow] [snowplow]**, **[Iglu] [iglu]**.

## User Quickstart

Download the latest Schema Guru from Bintray:

```bash
$ wget http://dl.bintray.com/snowplow/snowplow-generic/schema_guru_0.6.2.zip
$ unzip schema_guru_0.6.2.zip
$ mv schema-guru-0.6.2 schema-guru
```

Assuming you have a recent JVM installed.

### CLI

#### Schema derivation

You can use as input either single JSON file or directory with JSON instances (it will be processed recursively).

Following command will print JSON Schema to stdout:

```bash
$ ./schema-guru schema {{input}}
```

Also you can specify output file for your schema:

```bash
$ ./schema-guru schema --output {{json_schema_file}} {{input}}
```

You can also switch Schema Guru into **[NDJSON] [ndjson]** mode, where it will look for newline delimited JSON files:

```bash
$ ./schema-guru schema --ndjson {{input}}
```

You can specify the enum cardinality tolerance for your fields. It means that *all* fields which are found to have less than the specified cardinality will be specified in the JSON Schema using the `enum` property.

```bash
$ ./schema-guru schema --enum 5 {{input}}
```

If you know that some particular set of values can appear, but don't want to set big enum cardinality, you may want to specify predefined enum set with ``--enum-sets`` multi-option, like this:

```bash
$ ./schema-guru schema --enum-sets iso_4217 --enum-sets iso_3166-1_aplha-3 /path/to/instances
```

Currently Schema Guru includes following built-in enum sets (written as they should appear in CLI):

* [iso_4217] [iso-4217]
* [iso_3166-1_aplha-2] [iso-3166-1-alpha-2]
* [iso_3166-1_aplha-3] [iso-3166-1-alpha-3]
* Special `all` set, which mean all built-in enums will be included

If you need to include very specific enum set, you can define it by yourself in JSON file with array like this:

```json
["Mozilla Firefox", "Google Chrome", "Netscape Navigator", "Internet Explorer"]
```

And pass path to this file instead of enum name:

```bash
$ ./schema-guru schema --enum-sets all --enum-sets /path/to/browsers.json /path/to/instances
```

Schema Guru will derive `minLength` and `maxLength` properties for strings based on shortest and longest strings.
But this may be a problem if you process small amount of instances. 
To avoid this too strict Schema, you can use `--no-length` option.

```bash
$ ./schema-guru schema --no-length /path/to/few-instances
```

### Web UI

To run it locally:

```bash
$ wget http://dl.bintray.com/snowplow/snowplow-generic/schema_guru_webui_0.6.2.zip
$ unzip schema_guru_webui_0.6.2.zip
$ ./schema-guru-webui-0.6.2
```

The above will run a Spray web server containing Schema Guru on [0.0.0.0:8000] [webui-local]. Interface and port can be specified by `--interface` and `--port` respectively.

### Apache Spark

Since version 0.4.0 Schema Guru ships with a Spark job for deriving JSON Schemas.
To help users getting started with Schema Guru on Amazon Elastic MapReduce we provide [pyinvoke] [pyinvoke] ``tasks.py``.

The recommended way to start is install all requirements and assemble a fat JAR as described in the [developer quickstart](#developer-quickstart).

Before run you need:

* An AWS CLI profile, e.g. *my-profile*
* A EC2 key pair, e.g. *my-ec2-keypair*
* At least one Amazon S3 bucket, e.g. *my-bucket*

To provision the cluster and start the job you need to use `run_emr` task:

```bash
$ cd sparkjob
$ inv run_emr my-profile my-bucket/input/ my-bucket/output/ my-bucket/errors/ my-bucket/logs my-ec2-keypair
```

If you need some specific options for Spark job, you can specify these in `tasks.py`. 
The Spark job accepts the same options as the CLI application, but note that `--output` isn't optional and we have a new optional `--errors-path`.
Also, instead of specifying some of predefined enum sets you can just enable it with `--enum-sets` flag, so it has the same behaviour as `--enum-sets all`.

## Developer Quickstart

Assuming git, **[Vagrant] [vagrant-install]** and **[VirtualBox] [virtualbox-install]** installed:

```bash
 host$ git clone https://github.com/dataunitylab/schema-guru.git
 host$ cd schema-guru
 host$ vagrant up && vagrant ssh
guest$ cd /vagrant
guest$ sbt assembly
```

Also, optional:

```bash
guest$ sbt "project schema-guru-webui" assembly
guest$ sbt "project schema-guru-sparkjob" assembly
```

You can also deploy the Schema Guru web GUI onto Elastic Beanstalk:

```
guest$ cd beanstalk && zip beanstalk.zip *
```

Now just create a new Docker app in the **[Elastic Beanstalk Console] [beanstalk-console]** and upload this zip file.

## User Manual

### Functionality

#### Schema derivation

* Takes a directory as an argument and will print out the resulting JSON Schema:
  - Processes each JSON sequentially
  - Merges all results into one master JSON Schema
* Recognizes following JSON Schema formats:
  - UUID
  - date-time (according to ISO-8601)
  - IPv4 and IPv6 addresses
  - HTTP, HTTPS, FTP URLs
* Recognized `minLength` and `maxLength` properties for strings
* Recognizes Base64 pattern for strings
* Detects integer ranges according to `Int16`, `Int32`, `Int64`
* Detects misspelt properties and produce warnings
* Detects enum values with specified cardinality
* Detects known enum sets built-in or specified by user
* Allows to output **[Self-describing JSON Schema] [self-describing]**
* Allows to produce JSON Schemas with different names based on given JSON Path
* Supports **[Newline Delimited JSON] [ndjson]**

#### DDL derivation

* Correctly transforms some of string formats
  - UUID becomes ``CHAR(36)``
  - IPv4 becomes ``VARCHAR(14)``
  - IPv6 becomes ``VARCHAR(39)``
  - date-time becomes ``TIMESTAMP``
* Handles properties with only enums
* Property with ``maxLength(n)`` and ``minLength(n)`` becomes ``CHAR(n)``
* Can output a JSONPath file
* Can split product types
* Number with ``multiplyOf`` 0.01 becomes ``DECIMAL``
* Handles Self-describing JSON and can produce raw DDL
* Recognizes integer size by ``minimum`` and ``maximum`` values
* Object without ``properties``, but with ``patternProperties`` becomes ``VARCHAR(4096)``

### Assumptions

* All JSON files in the directory are assumed to be of the same event type and will be merged together
* All JSON files are assumed to start with either `{ ... }` or `[ ... ]`
  - If they do not they are discarded
* Schema should be as strict as possible - e.g. no `additionalProperties` are allowed currently

### Self-describing JSON
``schema`` command allows you to produce **[self-describing JSON Schema] [self-describing]**.
To produce it you need to specify vendor, name (if segmentation isn't using, see below), and version (optional, default value is 1-0-0).

```bash
$ ./schema-guru schema --vendor {{your_company}} --name {{schema_name}} --schemaver {{version}} {{input}}
```

### Schema Segmentation

If you have set of mixed JSON files from one vendor, but with slightly different structure, like:

```json
{ "version": 1,
  "type": "track",
  "userId": "019mr8mf4r",
  "event": "Purchased an Item",
  "properties": {
    "revenue": "39.95",
    "shippingMethod": "2-day" },
  "timestamp" : "2012-12-02T00:30:08.276Z" }
```

and

```json
{ "version": 1,
  "type": "track",
  "userId": "019mr8mf4r",
  "event": "Posted a Comment",
  "properties": {
    "body": "This book is gorgeous!",
    "attachment": false },
  "timestamp" : "2012-12-02T00:28:02.273Z" }
```

You can run it as follows:
```bash
$ ./schema-guru schema --output {{output_dir}} --schema-by $.event {{mixed_jsons_directory}}
```

It will put two or more JSON Schemas into the output directory: `Purchased_an_Item.json` and `Posted_a_comment.json`.
They will be derived from JSON files only with the corresponding event property, without any intersections.
(Assuming that the value at provided JSON Path contains a valid string).
All schemas where the value at the JSON Path is absent or does not contain a string value will be merged into `unmatched.json` schema in the same output directory.
Also, when self-describing JSON Schema is produced, it will take schema name in the same way and --name argument can be omitted (it will replace any name specified with the option).

### Example

Here's an example of some subtle points which a tool working with a single JSON instance would miss.

First instance:

```json
{ "event": {
    "just_a_string": "Any string may be here",
    "sometimes_ip": "192.168.1.101",
    "always_ipv4": "127.0.0.1",
    "id": 43,
    "very_big_int": 9223372036854775102,
    "this_should_be_number": 2.1,
    "nested_object": {
        "title": "Just an nested object",
        "date": "2015-05-29T12:00:00+07:00" }}}
```

Second instance:

```json
{ "event": {
    "just_a_string": "No particular format",
    "sometimes_ip": "This time it's not an IP",
    "always_ipv4": "192.168.1.101",
    "id": 42,
    "very_big_int": 92102,
    "this_should_be_number": 201,
    "not_always_here": 32,
    "nested_object": {
        "title": "Still plain string without format",
        "date": "1961-07-03T12:00:00+07:00" }}}
```

The generated schema:

```json
{ "type" : "object",
  "properties" : {
    "event" : {
      "type" : "object",
      "properties" : {
        "just_a_string" : { "type" : "string" },
        "sometimes_ip" : { "type" : "string" },
        "always_ipv4" : {
          "type" : "string",
          "format" : "ipv4" },
        "id" : {
          "type" : "integer",
          "minimum" : 0,
          "maximum" : 32767 },
        "very_big_int" : {
          "type" : "integer",
          "minimum" : 0,
          "maximum" : 9223372036854775807 },
        "this_should_be_number" : {
          "type" : "number",
          "minimum" : 0 },
        "nested_object" : {
          "type" : "object",
          "properties" : {
            "title" : { "type" : "string" },
            "date" : {
              "type" : "string",
              "format" : "date-time" } },
          "additionalProperties" : false },
        "not_always_here" : {
          "type" : "integer",
          "minimum" : 0,
          "maximum" : 32767 } },
      "additionalProperties" : false } },
  "additionalProperties" : false }
```

## Copyright and License

Schema Guru is copyright 2014-2016 Snowplow Analytics Ltd.

Licensed under the **[Apache License, Version 2.0] [license]** (the "License");
you may not use this software except in compliance with the License.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

[travis]: https://travis-ci.org/dataunitylab/schema-guru
[travis-image]: https://travis-ci.org/dataunitylab/schema-guru.png?branch=master

[license-image]: http://img.shields.io/badge/license-Apache--2-blue.svg?style=flat
[license]: http://www.apache.org/licenses/LICENSE-2.0

[release-image]: http://img.shields.io/badge/release-0.6.2-blue.svg?style=flat
[releases]: https://github.com/dataunitylab/schema-guru/releases

[json-schema]: http://json-schema.org/

[ndjson]: http://ndjson.org/
[ndjson-spec]: http://dataprotocols.org/ndjson/

[webui-local]: http://0.0.0.0:8000
[webui-hosted]: http://schemaguru.snowplowanalytics.com

[snowplow]: https://github.com/snowplow/snowplow
[iglu]: https://github.com/snowplow/iglu
[schema-ddl]: https://github.com/snowplow/schema-ddl
[self-describing]: http://snowplowanalytics.com/blog/2014/05/15/introducing-self-describing-jsons/

[redshift]: http://aws.amazon.com/redshift/
[redshift-copy]: http://docs.aws.amazon.com/redshift/latest/dg/r_COPY.html

[vagrant-install]: http://docs.vagrantup.com/v2/installation/index.html
[virtualbox-install]: https://www.virtualbox.org/wiki/Downloads
[pyinvoke]: http://www.pyinvoke.org/

[beanstalk-console]: http://console.aws.amazon.com/elasticbeanstalk

[iso-4217]: https://en.wikipedia.org/wiki/ISO_4217
[iso-3166-1-alpha-2]: https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2
[iso-3166-1-alpha-3]: https://en.wikipedia.org/wiki/ISO_3166-1_alpha-3
