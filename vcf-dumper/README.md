# European Variation Archive (EVA) VCF dumper

The VCF dumper is a web service that supports some queries from the [EVA core REST Webservices API](https://github.com/EBIvariation/eva-ws/wiki#variants) and writes the output in [VCF format](https://samtools.github.io/hts-specs/VCFv4.3.pdf).

## Dependencies
The VCF dumper has been implemented in Java 8 and uses Maven build system.

In order to run, it needs access to a MongoDB 3.x database instance. The easiest way to set one up in a local machine is [using Docker](https://hub.docker.com/_/mongo/).

The project dependencies are in maven central.

## Build
*Maven profiles* can be used to populate [eva.properties](vcf-dumper-lib/src/main/resources/eva.properties) with the MongoDB server details when the project is built. Not all properties are mandatory. This is an example of a maven profile for a local MongoDB server with no authentication:

```
<profile>
    <id>vcf-dumper-localhost</id>
    <properties>
        <eva.mongo.host>localhost:27017</eva.mongo.host>
        <eva.mongo.user></eva.mongo.user>
        <eva.mongo.passwd></eva.mongo.passwd>
        <eva.mongo.auth.db></eva.mongo.auth.db>
        <eva.mongo.collections.variants>variants</eva.mongo.collections.variants>
        <eva.mongo.collections.files>files</eva.mongo.collections.files>
    </properties>
</profile>
```

To build the project artifacts, execute `mvn package -P *chosen-profle*` in the vcf-dumper directory. An executable JAR will be generated in *vcf-dumper-cli/target* and a WAR file in *vcf-dumper-ws/target*. The WAR file has been tested successfully in Apache Tomcat 9.

## Test
In order to test the VCF dumper, a MongoDB server containing variants in [EVA format](https://github.com/EBIvariation/eva-pipeline/wiki/MongoDB-schema) is needed. Some small test databases dumps are provided in the [test resouces](vcf-dumper-lib/src/test/resources/db-dump). Those dumps can be imported to a MongoDB server using the `mongorestore` command. **Those databases can be deleted if the tests are executed** (e.g., by `maven package`). There are several solutions for this: rename those databases, skip the tests when executing maven or execute *mavenrestore* after building the artifacts.

Once we got a server with data, and a JAR or WAR artifact pointing to it, we can try some queries.

### Queries
The VCF dumper has two main interfaces that can be used to execute queries over the archive and get the result in VCF format:
* **WebServices**: Allows to dump the variants of some study(ies) that are in a given genomic region (or list of regions). Queries with no region are not allowed
* **Command Line Interface**: For EVA internal use only. Allows to dump all the variants for a given study(ies) and file(s). It does not include region filters

#### Webservices
The Webservices API currently comprises a single endpoint: `{baseURL}/{regionId}/variants`.
{regionId} can be a single region or a comma separated list of regions. Each region is a composed of a chromosome name, and optionally a ':' character followed by natural numbers pair (start and end), separated by '-'. Some examples of valid regions are:
* `1`
* `chr1`
* `MT`
* `1:1000-2000`
* `chr1:1000-2000`

And an example of a region list (whole chromosome 1, chromosome 2 positions 1000 to 2000, chromosome X positions 4000 to 5000) :
* `1,2:1000-2000,X:4000-5000`

The variants endpoint also includes some URL variables. Two are mandatory:
* **species**: the queried database name will be the species value prefixed by "eva_" (e.g., for species *hsapiens*, this service will fetch the data from a database named *eva_hsapiens*)
* **studies**: comma separated list of studies ids to query

In addition to those, there are several optional URL variables:
* annot-ct
* maf
* polyphen
* sift
* ref
* alt
* miss_alleles
* miss_gts

Any time a valid call is invoked, the client will receive a VCF file stream, containing the variants that satisfy the query criteria. 

#### CLI
The command line interface is intended to dump whole studies in VCF format. It does not allow filtering by region, so all the variants in the study will be dumped. This may be a time consuming operation, especially for big studies. 

This command line interface accepts the following parameters:
* **species**: species the data are associated with
* **database**: name of the database to extract data from
* **outdir**: output directory
* **studies**: comma separated list of studies to query
* **files**: comma separated list of files to query (each study in EVA can be composed of one or many files, as described [here](https://github.com/EBIvariation/eva-pipeline/wiki/MongoDB-schema#files)

A successful command execution will produce a VCF file in the output directory.

#### Querying the test data
To query the test databases, values must be assigned to the following, mandatory parameters:
* species
* studies
* files (for CLI queries)
* regions

There are three databases in the test data: `eva_hsapiens_test`, `eva_btaurus_umd32_test` and `eva_oaries_oarv31_test`. `hsapiens_test`,  `btaurus_umd32_test` and `oaries_oarv31_test` would be valid values for the species parameter then. 

To get the file and study IDs from a database, we can query the collection *files* using the Mongo shell:
```
> use eva_hsapiens_test
switched to db eva_hsapiens_test
> db.files.find({},{sid:1, fid:1})
{ "_id" : ObjectId("563cd52b9f44e8e80ea35828"), "fid" : "6", "sid" : "7" }
{ "_id" : ObjectId("563cd5439f44e8e80ea35829"), "fid" : "5", "sid" : "8" }
>
```
We can see in the query result that the study with id *7* is associated with the file with id *6*, and the study *8* with the file *5*. 

For the regions filter, we need to know which chromosomes have variants in the database and where (position of the variants in the chromosome). As described in the [MongoDB schema wiki](https://github.com/EBIvariation/eva-pipeline/wiki/MongoDB-schema), each variant has *chr* (chromosome) and *start* (position in the chromosome where the variant starts). We can figure out executing some queries in the *variants* collection:
```
> db.variants.distinct("chr", {"files.sid":"7"})
[ "20", "22" ]
> db.variants.find({"chr":"20", "files.sid":"7"},{"start":1}).sort({"start":1}).limit(1)
{ "_id" : "20_60343_G_A", "start" : 60343 }
> db.variants.find({"chr":"20", "files.sid":"7"},{"start":1}).sort({"start":-1}).limit(1)
{ "_id" : "20_71822_C_G", "start" : 71822 }
> db.variants.find({"chr":"22", "files.sid":"7"},{"start":1}).sort({"start":1}).limit(1)
{ "_id" : "22_16050075_A_G", "start" : 16050075 }
> db.variants.find({"chr":"22", "files.sid":"7"},{"start":1}).sort({"start":-1}).limit(1)
{ "_id" : "22_16110950_G_T", "start" : 16110950 }
```
We can see that the study *7* has variants in the chromosomes *20* (between position 60343 to 71822) and *22* (between 16060075 and 16110950). 

Now we have enough information to run some queries:

*Using the Webservices, get all the variants in some regions in study 7:*

GET call to `{baseURL}/v1/segments/20:65000-70000,22:16080000-16100000/variants?species=hsapiens_test&studies=7`

Note: for a local Tomcat running in 8080, deploying the war file produced by `maven install`, {baseURL} will be `http://localhost:8080/vcf-dumper/`

*Using the CLI, get all the variants in study 8:*

`java -jar {vcf-dumper-cli .jar file} --database eva_hsapiens_test --species hsapiens_test --studies 8 --files 5`
