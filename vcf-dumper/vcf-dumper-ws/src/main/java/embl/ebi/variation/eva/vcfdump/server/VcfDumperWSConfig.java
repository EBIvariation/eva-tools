package embl.ebi.variation.eva.vcfdump.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Created by pagarcia on 30/09/2016.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class VcfDumperWSConfig extends AsyncConfigurerSupport {

    @Override
    @Bean
    public Executor getAsyncExecutor() {
        // TODO: override those default values, or choose a different executor
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(5);
        pool.setMaxPoolSize(10);
        pool.setWaitForTasksToCompleteOnShutdown(true);
        return pool;
    }

}
