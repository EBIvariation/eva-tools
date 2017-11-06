/*
 * Copyright 2017 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.dbsnpimporter.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import uk.ac.ebi.eva.dbsnpimporter.DbsnpDatasource;

import javax.sql.DataSource;

public class DbsnpTestDatasource {

    private static final Logger logger = LoggerFactory.getLogger(DbsnpTestDatasource.class);

    public DataSource getDbsnpDatasource(Environment environment) {
        DataSource dataSource = new DbsnpDatasource().getDbsnpDatasource(environment);
        DatabasePopulatorUtils.execute(databasePopulator(environment), dataSource);
        return dataSource;
    }

    private DatabasePopulator databasePopulator(Environment environment) {
        final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new FileSystemResource(environment.getProperty("dbsnp.datasource.schema")));
        populator.addScript(new FileSystemResource(environment.getProperty("dbsnp.datasource.data")));
        return populator;
    }
}
