package embl.ebi.variation.eva.vcfdump.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Created by pagarcia on 30/09/2016.
 */
@Configuration
@EnableWebMvc
@EnableSwagger2
public class VcfDumperWSConfig extends WebMvcConfigurerAdapter {

    @Bean
    public ThreadPoolTaskExecutor mvcAsyncThreadPool(){
        // this pool will be used by to handle async requests in the MVC controllers
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        // TODO: override those default values
        pool.setCorePoolSize(5);
        pool.setMaxPoolSize(10);
        pool.setWaitForTasksToCompleteOnShutdown(true);
        return pool;
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(mvcAsyncThreadPool());
    }

    @Bean
    public Docket apiConfiguration() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("embl.ebi.variation.eva.vcfdump.server"))
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("European Variation Archive VCF Dumper REST Web Services API")
                .contact(new Contact("the European Variation Archive team", "www.ebi.ac.uk/eva", "eva-helpdesk@ebi.ac.uk"))
                .license("Apache License Version 2.0")
                .licenseUrl("https://www.apache.org/licenses/LICENSE-2.0")
                .version("1.0")
                .build();
    }

}
