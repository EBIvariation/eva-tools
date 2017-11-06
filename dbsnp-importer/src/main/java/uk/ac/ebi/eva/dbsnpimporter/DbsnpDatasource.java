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
package uk.ac.ebi.eva.dbsnpimporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

public class DbsnpDatasource {

    private static final Logger logger = LoggerFactory.getLogger(DbsnpDatasource.class);

    public DataSource getDbsnpDatasource(Environment environment) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(environment.getProperty("dbsnp.datasource.driver-class-name"));
        dataSource.setUrl(environment.getProperty("dbsnp.datasource.url"));
        dataSource.setUsername(environment.getProperty("dbsnp.datasource.username"));
        dataSource.setPassword(environment.getProperty("dbsnp.datasource.password"));
        return dataSource;
    }
}
