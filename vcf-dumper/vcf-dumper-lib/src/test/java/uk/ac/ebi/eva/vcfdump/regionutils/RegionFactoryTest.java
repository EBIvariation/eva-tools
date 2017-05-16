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

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.opencb.biodata.models.feature.Region;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.core.variant.adaptors.VariantDBAdaptor;

import uk.ac.ebi.eva.vcfdump.rules.TestDBRule;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RegionFactoryTest {

    private static VariantDBAdaptor variantDBAdaptor;

    // this is used for getting just one big region in 'full chromosome' tests
    private static final int BIG_WINDOW_SIZE = 100000000;

    @ClassRule
    public static TestDBRule mongoRule = new TestDBRule();

    @BeforeClass
    public static void setUpClass()
            throws IOException, InterruptedException, URISyntaxException, IllegalAccessException,
            ClassNotFoundException,
            InstantiationException, IllegalOpenCGACredentialsException {
        variantDBAdaptor = mongoRule.getVariantMongoDBAdaptor(TestDBRule.HUMAN_TEST_DB);
    }

    @Test
    public void getRegionsForChromosomeWhenEveryRegionInQueryContainsMinAndMaxCoordinates() {
        QueryOptions query = new QueryOptions(VariantDBAdaptor.REGION, "1:500-2499,2:100-300");
        RegionFactory regionFactory = new RegionFactory(1000, null, query);

        // chromosome that are in region list
        List<Region> chunks = regionFactory.getRegionsForChromosome("1");
        assertEquals(2, chunks.size());
        assertTrue(chunks.contains(new Region("1:500-1499")));
        assertTrue(chunks.contains(new Region("1:1500-2499")));
        chunks = regionFactory.getRegionsForChromosome("2");
        assertEquals(1, chunks.size());
        assertTrue(chunks.contains(new Region("2:100-300")));

        // chromosome that are not in region list
        chunks = regionFactory.getRegionsForChromosome("22");
        assertEquals(0, chunks.size());
    }

    @Test
    public void divideRegionInChunks() {
        RegionFactory regionFactory = new RegionFactory(1000, null, null);
        List<Region> regions = regionFactory.divideRegionInChunks("1", 500, 1499);
        assertTrue(regions.size() == 1);
        assertTrue(regions.contains(new Region("1", 500, 1499)));

        regions = regionFactory.divideRegionInChunks("1", 500, 1000);
        assertTrue(regions.size() == 1);
        assertTrue(regions.contains(new Region("1", 500, 1000)));

        regions = regionFactory.divideRegionInChunks("1", 2500, 2500);
        assertTrue(regions.size() == 1);
        assertTrue(regions.contains(new Region("1", 2500, 2500)));

        regions = regionFactory.divideRegionInChunks("1", 500, 2499);
        assertTrue(regions.size() == 2);
        assertTrue(regions.contains(new Region("1", 500, 1499)));
        assertTrue(regions.contains(new Region("1", 1500, 2499)));

        regions = regionFactory.divideRegionInChunks("1", 500, 2000);
        assertTrue(regions.size() == 2);
        assertTrue(regions.contains(new Region("1", 500, 1499)));
        assertTrue(regions.contains(new Region("1", 1500, 2000)));

        regions = regionFactory.divideRegionInChunks("1", 500, 2500);
        assertTrue(regions.size() == 3);
        assertTrue(regions.contains(new Region("1", 500, 1499)));
        assertTrue(regions.contains(new Region("1", 1500, 2499)));
        assertTrue(regions.contains(new Region("1", 2500, 2500)));

        regions = regionFactory.divideRegionInChunks("1", -1, 2500);
        assertTrue(regions.size() == 0);

        regions = regionFactory.divideRegionInChunks("1", 3000, 2500);
        assertTrue(regions.size() == 0);
    }

    @Test
    public void getRegionsForChromosomeWhenRegionQueryIsAFullChromosome()
            throws IOException, IllegalOpenCGACredentialsException {
        // the region filter is just the chromosome used for testing, with no coordinates
        QueryOptions query = new QueryOptions(VariantDBAdaptor.REGION, "22");
        RegionFactory regionFactory = new RegionFactory(BIG_WINDOW_SIZE, variantDBAdaptor, query);
        List<Region> regions = regionFactory.getRegionsForChromosome("22");
        assertTrue(regions.size() == 1);
        assertTrue(regions.contains(new Region("22", 16050075, 16110950)));
    }

    @Test
    public void getRegionsForChromosomeWhenRegionQueryStartsWithAFullChromosome()
            throws IOException, IllegalOpenCGACredentialsException {
        // the chromosome used for testing in in the first in the query, with no coordinates
        QueryOptions query = new QueryOptions(VariantDBAdaptor.REGION, "22,23:1000-2000");
        RegionFactory regionFactory = new RegionFactory(BIG_WINDOW_SIZE, variantDBAdaptor, query);
        List<Region> regions = regionFactory.getRegionsForChromosome("22");
        assertTrue(regions.size() == 1);
        assertTrue(regions.contains(new Region("22", 16050075, 16110950)));
    }

    @Test
    public void getRegionsForChromosomeWhenRegionQueryEndsWithAFullChromosome()
            throws IOException, IllegalOpenCGACredentialsException {
        // the chromosome used for testing in in the last in the query, with no coordinates
        QueryOptions query = new QueryOptions(VariantDBAdaptor.REGION, "1:500-2499,22");
        RegionFactory regionFactory = new RegionFactory(BIG_WINDOW_SIZE, variantDBAdaptor, query);
        List<Region> regions = regionFactory.getRegionsForChromosome("22");
        assertTrue(regions.size() == 1);
        assertTrue(regions.contains(new Region("22", 16050075, 16110950)));
    }

    @Test
    public void getRegionsForChromosomeWhenRegionQueryContainsAFullChromosome()
            throws IOException, IllegalOpenCGACredentialsException {
        // the chromosome used for testing in the middle of the query, with no coordinates
        QueryOptions query = new QueryOptions(VariantDBAdaptor.REGION, "1:500-2499,22,21:1000-2000");
        RegionFactory regionFactory = new RegionFactory(BIG_WINDOW_SIZE, variantDBAdaptor, query);
        List<Region> regions = regionFactory.getRegionsForChromosome("22");
        assertTrue(regions.size() == 1);
        assertTrue(regions.contains(new Region("22", 16050075, 16110950)));
    }

}