/*
 * Copyright 2015 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.vcfdump.regionutils;

import org.junit.Test;

import uk.ac.ebi.eva.commons.core.models.Region;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class IntersectingRegionsMergerTest {

    @Test
    public void testNoIntersectingRegionList() {
        List<Region> nonIntersectingRegions = Arrays
                .asList(new Region("1", 100L, 200L), new Region("1", 300L, 500L), new Region("2", 150L, 250L));
        List<Region> mergedRegions = nonIntersectingRegions.stream().collect(new IntersectingRegionsMerger());
        assertEquals(nonIntersectingRegions, mergedRegions);
    }

    @Test
    public void testSortedOutput() {
        List<Region> unsortedRegions = Arrays
                .asList(new Region("1", 300L, 500L), new Region("2", 150L, 250L), new Region("1", 100L, 200L));
        List<Region> mergedRegions = unsortedRegions.stream().collect(new IntersectingRegionsMerger());
        assertEquals(Arrays.asList(new Region("1", 100L, 200L), new Region("1", 300L, 500L), new Region("2", 150L, 250L)),
                     mergedRegions);
    }

    @Test
    public void testMergeIntersectingRegions() {
        List<Region> intersectingRegions = Arrays
                .asList(new Region("1", 100L, 200L), new Region("1", 150L, 500L), new Region("2", 150L, 250L));
        List<Region> mergedRegions = intersectingRegions.stream().collect(new IntersectingRegionsMerger());
        assertEquals(Arrays.asList(new Region("1", 100L, 500L), new Region("2", 150L, 250L)), mergedRegions);
    }

    @Test
    public void testMergeUnsortedIntersectingRegions() {
        List<Region> intersectingRegions = Arrays
                .asList(new Region("1", 150L, 500L), new Region("1", 100L, 200L), new Region("2", 150L, 250L));
        List<Region> mergedRegions = intersectingRegions.stream().collect(new IntersectingRegionsMerger());
        assertEquals(Arrays.asList(new Region("1", 100L, 500L), new Region("2", 150L, 250L)), mergedRegions);
    }

    @Test
    public void testMergeRegionContainedInOther() {
        List<Region> intersectingRegions = Arrays.asList(new Region("1", 100L, 300L), new Region("1", 150L, 250L));
        List<Region> mergedRegions = intersectingRegions.stream().collect(new IntersectingRegionsMerger());
        assertEquals(Collections.singletonList(new Region("1", 100L, 300L)), mergedRegions);
    }

    @Test
    public void testMoreThanTwoIntersectingRegions() {
        List<Region> intersectingRegions =
                Arrays.asList(new Region("1", 100L, 200L), new Region("1", 150L, 500L), new Region("1", 400L, 600L),
                              new Region("1", 125L, 550L));
        List<Region> mergedRegions = intersectingRegions.stream().collect(new IntersectingRegionsMerger());
        assertEquals(Collections.singletonList(new Region("1", 100L, 600L)), mergedRegions);
    }

    @Test
    public void testMergeTwoIdenticalRegions() {
        List<Region> intersectingRegions =
                Arrays.asList(new Region("1", 100L, 200L), new Region("1", 100L, 200L));
        List<Region> mergedRegions = intersectingRegions.stream().collect(new IntersectingRegionsMerger());
        assertEquals(Collections.singletonList(new Region("1", 100L, 200L)), mergedRegions);
    }

}