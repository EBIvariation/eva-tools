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
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.autoconfigure.batch.JobLauncherCommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class DbsnpImporterJobLauncherCommandLineRunner extends JobLauncherCommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DbsnpImporterJobLauncherCommandLineRunner.class);

    private JobParameters jobParameters;

    public DbsnpImporterJobLauncherCommandLineRunner(JobLauncher jobLauncher, JobExplorer jobExplorer,
                                                     JobParameters jobParameters) {
        super(jobLauncher, jobExplorer);
        this.jobParameters = jobParameters;
    }

    @Override
    public void run(String... args) throws JobExecutionException {
        Properties parameters = jobParameters.toProperties();
        logger.info("Running default command line with: " + parameters.toString());
        launchJobFromProperties(parameters);
    }
}
