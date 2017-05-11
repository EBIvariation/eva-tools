/*
 * Copyright 2016-2017 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.eva.dbmigration.mongodb;

/**
 * Container of credentials for a connection to mongo.
 */
public class DatabaseParameters {

    public static final String DB_HOST = "db.host";

    public static final String DB_AUTHENTICATION_DATABASE = "db.authentication-database";

    public static final String DB_USERNAME = "db.username";

    public static final String DB_PASSWORD = "db.password";

    public static final String DB_READ_PREFERENCE = "db.read-preference";

    public static final String DB_DATABASE = "db.database";

    public static final String DB_COLLECTIONS_VARIANTS_NAME = "db.collections.variants.name";

    public static final String DB_COLLECTIONS_FILES_NAME = "db.collections.files.name";

    public static final String DB_COLLECTIONS_FEATURES_NAME = "db.collections.features.name";

    public static final String DB_COLLECTIONS_STATISTICS_NAME = "db.collections.stats.name";

    public static final String DB_COLLECTIONS_ANNOTATIONS_NAME = "db.collections.annotations.name";

    public static final String DB_COLLECTIONS_ANNOTATION_METADATA_NAME = "db.collections.annotation-metadata.name";

}
