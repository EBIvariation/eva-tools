package uk.ac.ebi.eva.countstats.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import uk.ac.ebi.eva.countstats.model.Count;
import uk.ac.ebi.eva.countstats.repository.CountRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {CountStatsIntegrationTest.Initializer.class})
public class CountStatsIntegrationTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CountRepository countRepository;

    @ClassRule
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:9.6");

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues
                    .of("spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                            "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                            "spring.datasource.password=" + postgreSQLContainer.getPassword())
                    .applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    @Test
    public void testSaveCount() throws Exception {
        Count count1 = new Count(1L, "VARIANT_WAREHOUSE_INGESTION", "{\"study\": \"PRJ11111\", \"analysis\": \"ERZ11111\", \"batch\":1}",
                "INSERTED_VARIANTS", 10000);
        Count count2 = new Count(2L, "VARIANT_WAREHOUSE_INGESTION", "{\"study\": \"PRJ11111\", \"analysis\": \"ERZ11111\", \"batch\":1}",
                "INSERTED_VARIANTS", 15000);

        mvc.perform(post("/v1/countstats/count")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(count1)))
                .andExpect(status().isOk());
        mvc.perform(post("/v1/countstats/count")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(count2)))
                .andExpect(status().isOk());

        Optional<Count> resCount1 = countRepository.findById(1L);
        assertThat(resCount1.get()).isNotNull();
        assertThat(resCount1.get().getCount()).isEqualTo(10000);

        Optional<Count> resCount2 = countRepository.findById(2L);
        assertThat(resCount2.get()).isNotNull();
        assertThat(resCount2.get().getCount()).isEqualTo(15000);

        Long totalCount = countRepository.getCountForProcess("VARIANT_WAREHOUSE_INGESTION", "PRJ11111");
        assertThat(totalCount).isEqualTo(25000);
    }
}