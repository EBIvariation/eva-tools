# European Variation Archive (EVA) VCF dumper

The VCF dumper is a web service that supports some queries from the [EVA core REST Webservices API](https://github.com/EBIvariation/eva-ws/wiki#variants "EVA WS Variants endpoints") and writes the output in [VCF format](https://samtools.github.io/hts-specs/VCFv4.3.pdf "VCF format specification")

## Build
In order to build the VCF dumper, you need to install the Java Development Kit 8 and Maven.

The project dependencies are in maven central, excluding OpenCGA. You can get OpenCGA 0.5.2 from [here](https://github.com/opencb/opencga/tree/hotfix/0.5), branch `hotfix/0.5`. Please follow the download/compilation instructions there.

The VCF dumper will extract the variants from a MongoDB server. *Maven profiles* can be used to populate [eva.properties](vcf-dumper-lib/src/main/resources/eva.properties) with the desired database server details when the project is built. Not all properties are mandatory: this is an example of a maven profile for a local MongoDB server with no authentication:

```<profile>
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

To build the project artifacts, execute `mvn package -P *chosen-profle*` in the vcf-dumper directory. An executable jar will be generated in *vcf-dumper-cli/target* and a war file in *vcf-dumper-ws/target*. The war file has been tested successfully in Apache Tomcat 9.

## Test
In order to test the VCF dumper, a MongoDB server containing variants in [EVA format](https://github.com/EBIvariation/eva-pipeline/wiki/MongoDB-schema "EVA MongoDB schema") is needed. Some small test databases dumps are provided in the [test resouces](vcf-dumper-lib/src/test/resources/dump). Those dumps can be imported to a MongoDB server using the `mongorestore` command. **Those databases can be deleted if the tests are executed** (e.g., by `maven package`). There are several solutions for this: rename those databases, skip the tests when executing maven or execute *mavenrestore* after building the artifacts.

Once we got a server with data, and a jar or war artifact pointing to it, we can try some queries.

## Queries
TODO

### Webservices
TODO

### CLI
TODO
