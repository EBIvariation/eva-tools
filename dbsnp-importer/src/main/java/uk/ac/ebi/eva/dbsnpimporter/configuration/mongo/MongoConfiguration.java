/*
 * Copyright 2018 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.dbsnpimporter.configuration.mongo;

import com.mongodb.MongoClient;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import uk.ac.ebi.eva.dbsnpimporter.configuration.DBAdaptorConnector;

import java.net.UnknownHostException;

@Configuration
@EnableMongoRepositories(basePackages = "uk.ac.ebi.eva.commons.mongodb.repositories")
@EntityScan(basePackages = "uk.ac.ebi.eva.commons.mongodb.services")
@EnableMongoAuditing
@Import(SpringDataMongoDbProperties.class)
public class MongoConfiguration {

    @Bean
    public MappingMongoConverter mappingMongoConverter(MongoDbFactory factory, MongoMappingContext context) {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
        MappingMongoConverter mappingConverter = new MappingMongoConverter(dbRefResolver, context);
        mappingConverter.setMapKeyDotReplacement("£");
        return mappingConverter;
    }

    @Bean
    public MongoClient mongoClient(
            SpringDataMongoDbProperties springDataMongoDbProperties) throws UnknownHostException {
        return DBAdaptorConnector.getMongoClient(springDataMongoDbProperties);
    }
}
