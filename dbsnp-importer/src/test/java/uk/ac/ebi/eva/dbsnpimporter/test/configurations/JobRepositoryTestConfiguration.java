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
package uk.ac.ebi.eva.dbsnpimporter.test.configurations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.MapJobExplorerFactoryBean;
import org.springframework.batch.core.explore.support.SimpleJobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import static uk.ac.ebi.eva.dbsnpimporter.test.configurations.TestConfiguration.JOB_REPOSITORY_DATA_SOURCE;

@Configuration
public class JobRepositoryTestConfiguration
        extends DefaultBatchConfigurer
//        implements BatchConfigurer
{

    private static final Logger logger = LoggerFactory.getLogger(JobRepositoryTestConfiguration.class);

    public JobRepositoryTestConfiguration(@Autowired @Qualifier(JOB_REPOSITORY_DATA_SOURCE) DataSource dataSource) {
        super.setDataSource(dataSource);
    }

    //    private Environment environment;
//
//    public JobRepositoryTestConfiguration() {
//        super();
//    }
//
    @Override
    public void setDataSource(DataSource ignored) {
        logger.info("Ignoring datasource " + ignored.toString() + " for job repository, using in-memory jobRepository");
    }

//    @Bean
//    public ResourcelessTransactionManager getTransactionManager() {
//        return new ResourcelessTransactionManager();
//    }
//
//    @Bean
//    public JobRepository jobRepository(ResourcelessTransactionManager transactionManager) throws Exception {
//        MapJobRepositoryFactoryBean mapJobRepositoryFactoryBean = new MapJobRepositoryFactoryBean(transactionManager);
//        mapJobRepositoryFactoryBean.setTransactionManager(transactionManager);
//        return mapJobRepositoryFactoryBean.getObject();
//    }

//    @Bean
//    public SimpleJobLauncher jobLauncher(JobRepository jobRepository) {
//        SimpleJobLauncher simpleJobLauncher = new SimpleJobLauncher();
//        simpleJobLauncher.setJobRepository(jobRepository);
//        return simpleJobLauncher;
//    }
//

/*
    @Bean
    public ResourcelessTransactionManager transactionManager() {
        return new ResourcelessTransactionManager();
    }

    @Bean
    public MapJobRepositoryFactoryBean mapJobRepositoryFactory(ResourcelessTransactionManager transactionManager)
            throws Exception {
        MapJobRepositoryFactoryBean factory = new MapJobRepositoryFactoryBean(transactionManager);
        factory.afterPropertiesSet();
        return factory;
    }

    @Bean
    public JobRepository jobRepository(MapJobRepositoryFactoryBean repositoryFactory) throws Exception {
        return repositoryFactory.getObject();
    }

    @Bean
    public JobExplorer jobExplorer(MapJobRepositoryFactoryBean repositoryFactory) {
        return new SimpleJobExplorer(repositoryFactory.getJobInstanceDao(), repositoryFactory.getJobExecutionDao(),
                                     repositoryFactory.getStepExecutionDao(), repositoryFactory.getExecutionContextDao());
    }
*/
//    @Bean
//    public SimpleJobLauncher jobLauncher(JobRepository jobRepository) {
//        SimpleJobLauncher launcher = new SimpleJobLauncher();
//        launcher.setJobRepository(jobRepository);
//        return launcher;
//    }

//    private PlatformTransactionManager transactionManager;
//
//    private JobRepository jobRepository;
//
//    private JobLauncher jobLauncher;
//
//    private JobExplorer jobExplorer;
//
//    @Override
//    public PlatformTransactionManager getTransactionManager() {
//        return transactionManager;
//    }
//
//    @Override
//    public JobRepository getJobRepository() {
//        return jobRepository;
//    }
//
//    @Override
//    public JobExplorer getJobExplorer() {
//        return jobExplorer;
//    }
//
//    @Override
//    public JobLauncher getJobLauncher() {
//        return jobLauncher;
//    }
//
//    @PostConstruct
//    public void initialize() throws Exception {
//        logger.warn("No datasource was provided...using a Map based JobRepository");
//
//        if (this.transactionManager == null) {
//            this.transactionManager = new ResourcelessTransactionManager();
//        }
//
//        MapJobRepositoryFactoryBean jobRepositoryFactory = new MapJobRepositoryFactoryBean(this.transactionManager);
//        jobRepositoryFactory.afterPropertiesSet();
//        this.jobRepository = jobRepositoryFactory.getObject();
//
//        MapJobExplorerFactoryBean jobExplorerFactory = new MapJobExplorerFactoryBean(jobRepositoryFactory);
//        jobExplorerFactory.afterPropertiesSet();
//        this.jobExplorer = jobExplorerFactory.getObject();
//
//        this.jobLauncher = createJobLauncher();
//    }
//
//    private JobLauncher createJobLauncher() throws Exception {
//        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
//        jobLauncher.setJobRepository(jobRepository);
//        jobLauncher.afterPropertiesSet();
//        return jobLauncher;
//    }
}
