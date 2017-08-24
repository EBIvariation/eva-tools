package uk.ac.ebi.eva.dbsnpimporter.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

@Configuration
@PropertySource("classpath:application.properties")
public class TestDataSourceConfiguration {

    @Autowired
    private Environment env;

    @Bean
    public DataSource inMemoryDatasource() {
        return DataSourceBuilder.create().driverClassName(env.getProperty("spring.datasource.driver-class-name"))
                                .url(env.getProperty("spring.datasource.url"))
                                .username(env.getProperty("spring.datasource.username"))
                                .password(env.getProperty("spring.datasource.password")).build();
    }

    @Bean
    public DatabasePopulator inMemoryDatasourcePopulator() {
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
        databasePopulator.setContinueOnError(false);
        databasePopulator.addScript(new ClassPathResource("testschema.sql"));
        return databasePopulator;
    }
}
