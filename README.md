# Schema Guru

[![Build Status](https://travis-ci.com/dataunitylab/schema-guru.svg?branch=master)](https://travis-ci.com/dataunitylab/schema-guru)
[![Release](http://img.shields.io/badge/release-0.6.2-blue.svg?style=flat)](https://github.com/dataunitylab/schema-guru/releases)
[![License](http://img.shields.io/badge/license-Apache--2-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)

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

Assuming git, [pyinvoke]: http://www.pyinvoke.org/

[beanstalk-console]: http://console.aws.amazon.com/elasticbeanstalk

[iso-4217]: https://en.wikipedia.org/wiki/ISO_4217
[iso-3166-1-alpha-2]: https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2
[iso-3166-1-alpha-3]: https://en.wikipedia.org/wiki/ISO_3166-1_alpha-3
