# EVA Database migration (MongoBee)

EVA database-migration is a project to update the schema of the MongoDB
databases used in EVA.

It uses MongoBee, which applies a list of user functions that update the schema,
and tracks which functions were applied to each database.

## Build

This project does not require more than the usual `mvn clean compile`
(or `mvn clean install`).

## Run

The migration needs MongoDB credentials, which should be specified through
a properties file (e.g. `your-credentials.properties`), and passed as
unique parameter like this:

```
$ java -jar target/database-migration-0.2-SNAPSHOT-jar-with-dependencies.jar your-credentials.properties`
```

The available parameters are shown in a template properties in
eva-tools/database-migration/src/main/resources/example-mongodb.properties:

```
# Required parameters
db.name=your_db
db.read-preference=primary
db.collections.variants.name=variants_migration
db.collections.annotations.name=annotations_migration
db.collections.annotation-metadata.name=annotationMetadata_migration

vep.version=78
vep.cache.version=78

# Optional parameters
db.hosts=your_host
db.port=27017
db.authentication-database=admin
db.username=your_user
db.password=your_pass
```
