/*
 *
 *  * Copyright 2016 EMBL - European Bioinformatics Institute
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package uk.ac.ebi.eva.vcfdump.regionutils;

import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.ebi.eva.commons.core.models.Region;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;
import uk.ac.ebi.eva.vcfdump.MongoRepositoryTestConfiguration;
import uk.ac.ebi.eva.vcfdump.QueryParams;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MongoRepositoryTestConfiguration.class})
@UsingDataSet(locations = {
        "/db-dump/eva_hsapiens_grch37/files_2_0.json",
        "/db-dump/eva_hsapiens_grch37/variants_2_0.json"})
public class RegionFactoryTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("test-db");

    @Autowired
    private VariantWithSamplesAndAnnotationsService variantService;

    // this is used for getting just one big region in 'full chromosome' tests
    private static final int BIG_WINDOW_SIZE = 100000000;

    @BeforeClass
    public static void setUpClass()
            throws IOException, InterruptedException, URISyntaxException, IllegalAccessException,
            ClassNotFoundException,
            InstantiationException {
    }

    @Test
    public void getRegionsForChromosomeWhenEveryRegionInQueryContainsMinAndMaxCoordinates() {
        QueryParams query = new QueryParams();
        query.setRegion("1:500-2499,2:100-300");
        query.setStudies(Arrays.asList("7", "8"));
        RegionFactory regionFactory = new RegionFactory(1000, variantService);

        // chromosome that are in region list
        List<Region> chunks = regionFactory.getRegionsForChromosome("1", query);
        assertEquals(2, chunks.size());
        assertTrue(chunks.contains(new Region("1:500-1499")));
        assertTrue(chunks.contains(new Region("1:1500-2499")));
        chunks = regionFactory.getRegionsForChromosome("2", query);
        assertEquals(1, chunks.size());
        assertTrue(chunks.contains(new Region("2:100-300")));

        // chromosome that are not in region list
        chunks = regionFactory.getRegionsForChromosome("22", query);
        assertEquals(0, chunks.size());
    }

    @Test
    public void divideRegionInChunks() {
        RegionFactory regionFactory = new RegionFactory(1000, variantService);
        List<Region> regions = regionFactory.divideRegionInChunks("1", 500, 1499);
        assertTrue(regions.size() == 1);
        assertTrue(regions.contains(new Region("1", 500L, 1499L)));

        regions = regionFactory.divideRegionInChunks("1", 500L, 1000L);
        assertTrue(regions.size() == 1);
        assertTrue(regions.contains(new Region("1", 500L, 1000L)));

        regions = regionFactory.divideRegionInChunks("1", 2500L, 2500L);
        assertTrue(regions.size() == 1);
        assertTrue(regions.contains(new Region("1", 2500L, 2500L)));

        regions = regionFactory.divideRegionInChunks("1", 500L, 2499L);
        assertTrue(regions.size() == 2);
        assertTrue(regions.contains(new Region("1", 500L, 1499L)));
        assertTrue(regions.contains(new Region("1", 1500L, 2499L)));

        regions = regionFactory.divideRegionInChunks("1", 500L, 2000L);
        assertTrue(regions.size() == 2);
        assertTrue(regions.contains(new Region("1", 500L, 1499L)));
        assertTrue(regions.contains(new Region("1", 1500L, 2000L)));

        regions = regionFactory.divideRegionInChunks("1", 500L, 2500L);
        assertTrue(regions.size() == 3);
        assertTrue(regions.contains(new Region("1", 500L, 1499L)));
        assertTrue(regions.contains(new Region("1", 1500L, 2499L)));
        assertTrue(regions.contains(new Region("1", 2500L, 2500L)));

        regions = regionFactory.divideRegionInChunks("1", -1, 2500);
        assertTrue(regions.size() == 0);

        regions = regionFactory.divideRegionInChunks("1", 3000, 2500);
        assertTrue(regions.size() == 0);
    }

    @Test
    public void getRegionsForChromosomeWhenRegionQueryIsAFullChromosome()
            throws IOException {
        // the region filter is just the chromosome used for testing, with no coordinates
        QueryParams query = new QueryParams();
        query.setRegion("22");
        query.setStudies(Arrays.asList("7", "8"));
        RegionFactory regionFactory = new RegionFactory(BIG_WINDOW_SIZE, variantService);
        List<Region> regions = regionFactory.getRegionsForChromosome("22", query);
        assertTrue(regions.size() == 1);
        assertTrue(regions.contains(new Region("22", 16050075L, 16110950L)));
    }

    @Test
    public void getRegionsForChromosomeWhenRegionQueryStartsWithAFullChromosome()
            throws IOException {
        // the chromosome used for testing in in the first in the query, with no coordinates
        QueryParams query = new QueryParams();
        query.setRegion("22,23:1000-2000");
        query.setStudies(Arrays.asList("7", "8"));
        RegionFactory regionFactory = new RegionFactory(BIG_WINDOW_SIZE, variantService);
        List<Region> regions = regionFactory.getRegionsForChromosome("22", query);
        assertTrue(regions.size() == 1);
        assertTrue(regions.contains(new Region("22", 16050075L, 16110950L)));
    }

    @Test
    public void getRegionsForChromosomeWhenRegionQueryEndsWithAFullChromosome()
            throws IOException {
        // the chromosome used for testing in in the last in the query, with no coordinates
        QueryParams query = new QueryParams();
        query.setRegion("1:500-2499,22");
        query.setStudies(Arrays.asList("7", "8"));
        RegionFactory regionFactory = new RegionFactory(BIG_WINDOW_SIZE, variantService);
        List<Region> regions = regionFactory.getRegionsForChromosome("22", query);
        assertTrue(regions.size() == 1);
        assertTrue(regions.contains(new Region("22", 16050075L, 16110950L)));
    }

    @Test
    public void getRegionsForChromosomeWhenRegionQueryContainsAFullChromosome()
            throws IOException {
        // the chromosome used for testing in the middle of the query, with no coordinates
        QueryParams query = new QueryParams();
        query.setRegion("1:500-2499,22,21:1000-2000");
        query.setStudies(Arrays.asList("7", "8"));
        RegionFactory regionFactory = new RegionFactory(BIG_WINDOW_SIZE, variantService);
        List<Region> regions = regionFactory.getRegionsForChromosome("22", query);
        assertTrue(regions.size() == 1);
        assertTrue(regions.contains(new Region("22", 16050075L, 16110950L)));
    }

}