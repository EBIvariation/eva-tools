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

import java.util.Properties;

/**
 * Container of credentials for a connection to mongo.
 */
public class DatabaseParameters {

    static final String DB_HOSTS = "db.hosts";
    private String dbHosts;

    static final String DB_PORT = "db.port";
    private String dbPort;

    static final String DB_AUTHENTICATION_DATABASE = "db.authentication-database";
    private String dbAuthenticationDatabase;

    static final String DB_USERNAME = "db.username";
    private String dbUsername;

    static final String DB_PASSWORD = "db.password";
    private String dbPassword;

    static final String DB_READ_PREFERENCE = "db.read-preference";
    private String dbReadPreference;

    static final String DB_NAME = "db.name";
    private String dbName;

    static final String DB_COLLECTIONS_VARIANTS_NAME = "db.collections.variants.name";
    private String dbCollectionsVariantsName;

    static final String DB_COLLECTIONS_FILES_NAME = "db.collections.files.name";
    private String dbCollectionsFilesName;

    static final String DB_COLLECTIONS_FEATURES_NAME = "db.collections.features.name";
    private String dbCollectionsFeaturesName;

    static final String DB_COLLECTIONS_STATISTICS_NAME = "db.collections.stats.name";
    private String dbCollectionsStatisticsName;

    static final String DB_COLLECTIONS_ANNOTATIONS_NAME = "db.collections.annotations.name";
    private String dbCollectionsAnnotationsName;

    static final String DB_COLLECTIONS_ANNOTATION_METADATA_NAME = "db.collections.annotation-metadata.name";
    private String dbCollectionsAnnotationMetadataName;

    static final String VEP_VERSION = "vep.version";
    private String vepVersion;

    static final String VEP_CACHE_VERSION = "vep.cache.version";
    private String vepCacheVersion;

    public void load(Properties properties) {
        getRequiredProperties(properties);
        getOptionalProperties(properties);
    }

    private void getRequiredProperties(Properties properties) {
        dbName = getRequiredProperty(properties, DB_NAME);
        dbCollectionsVariantsName = getRequiredProperty(properties, DB_COLLECTIONS_VARIANTS_NAME);
        dbCollectionsAnnotationsName = getRequiredProperty(properties, DB_COLLECTIONS_ANNOTATIONS_NAME);
        dbCollectionsAnnotationMetadataName = getRequiredProperty(properties, DB_COLLECTIONS_ANNOTATION_METADATA_NAME);
        vepVersion = getRequiredProperty(properties, VEP_VERSION);
        vepCacheVersion = getRequiredProperty(properties, VEP_CACHE_VERSION);
    }

    private String getRequiredProperty(Properties properties, String propertyKey) {
        String property = properties.getProperty(propertyKey);
        if (property == null || property.isEmpty()) {
            throw new IllegalArgumentException("Parameter " + propertyKey + " is required");
        }
        return property;
    }

    private void getOptionalProperties(Properties properties) {
        dbHosts = properties.getProperty(DB_HOSTS);
        dbPort = properties.getProperty(DB_PORT);
        dbAuthenticationDatabase = properties.getProperty(DB_AUTHENTICATION_DATABASE);
        dbUsername = properties.getProperty(DB_USERNAME);
        dbPassword = properties.getProperty(DB_PASSWORD);
        dbReadPreference = properties.getProperty(DB_READ_PREFERENCE);
        dbCollectionsFilesName = properties.getProperty(DB_COLLECTIONS_FILES_NAME);
        dbCollectionsFeaturesName = properties.getProperty(DB_COLLECTIONS_FEATURES_NAME);
        dbCollectionsStatisticsName = properties.getProperty(DB_COLLECTIONS_STATISTICS_NAME);
    }

    public String getDbHosts() {
        return dbHosts;
    }

    public String getDbPort() {
        return dbPort;
    }

    public String getDbAuthenticationDatabase() {
        return dbAuthenticationDatabase;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public String getDbReadPreference() {
        return dbReadPreference;
    }

    public String getDbName() {
        return dbName;
    }

    public String getDbCollectionsVariantsName() {
        return dbCollectionsVariantsName;
    }

    public String getDbCollectionsFilesName() {
        return dbCollectionsFilesName;
    }

    public String getDbCollectionsFeaturesName() {
        return dbCollectionsFeaturesName;
    }

    public String getDbCollectionsStatisticsName() {
        return dbCollectionsStatisticsName;
    }

    public String getDbCollectionsAnnotationsName() {
        return dbCollectionsAnnotationsName;
    }

    public String getDbCollectionsAnnotationMetadataName() {
        return dbCollectionsAnnotationMetadataName;
    }

    public String getVepVersion() {
        return vepVersion;
    }

    public String getVepCacheVersion() {
        return vepCacheVersion;
    }
}
