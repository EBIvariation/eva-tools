package uk.ac.ebi.eva.dbsnpimporter.io.readers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.dbsnpimporter.models.Sample;
import uk.ac.ebi.eva.dbsnpimporter.test.DbsnpTestDatasource;
import uk.ac.ebi.eva.dbsnpimporter.test.configuration.TestConfiguration;

import javax.sql.DataSource;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@TestPropertySource({"classpath:application.properties"})
@ContextConfiguration(classes = {TestConfiguration.class})
public class WindingItemReaderTest {

    private static final int PAGE_SIZE = 2000;

    public static final int BATCH_ID = 11825;

    private DataSource dataSource;

    @Autowired
    private DbsnpTestDatasource dbsnpTestDatasource;

    @Before
    public void setUp() throws Exception {
        dataSource = dbsnpTestDatasource.getDatasource();
    }

    @Test
    public void shouldReadAllSamplesInBatchInSingleOperation() throws Exception {
        SampleReader reader = buildReader(BATCH_ID, PAGE_SIZE);
        consumeReader(new WindingItemReader<>(reader), 2);
        reader.close();
    }

    private SampleReader buildReader(int batch, int pageSize) throws Exception {
        SampleReader fieldsReader = new SampleReader(batch, dataSource, pageSize);
        fieldsReader.afterPropertiesSet();
        ExecutionContext executionContext = new ExecutionContext();
        fieldsReader.open(executionContext);
        return fieldsReader;
    }

    private void consumeReader(ItemReader<List<Sample>> reader, long expectedCount) throws Exception {
        long numReadOperations = 0;
        long numReadItems = 0;
        List<Sample> readItems = null;

        // consume all the items provided by the reader
        while ((readItems = reader.read()) != null) {
            numReadOperations++;
            numReadItems += readItems.size();
        }

        // assertThat(expectedCount, lessThanOrEqualTo(count));
        assertEquals(1, numReadOperations);
        assertEquals(expectedCount, numReadItems);
    }

}
