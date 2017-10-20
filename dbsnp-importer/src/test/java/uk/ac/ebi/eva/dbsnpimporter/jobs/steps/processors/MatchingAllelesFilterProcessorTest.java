package uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.eva.dbsnpimporter.models.LocusType;
import uk.ac.ebi.eva.dbsnpimporter.models.Orientation;
import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.ac.ebi.eva.dbsnpimporter.test.TestUtils.assertContains;

public class MatchingAllelesFilterProcessorTest {

    private List<SubSnpCoreFields> matchingAllelesVariants;

    private List<SubSnpCoreFields> mismatchingAllelesVariants;

    private MatchingAllelesFilterProcessor filter;

    @Before
    public void setUp() throws Exception {
        filter = new MatchingAllelesFilterProcessor();
        matchingAllelesVariants = new ArrayList<>();
        matchingAllelesVariants
                .add(new SubSnpCoreFields(26201546L, Orientation.FORWARD, 13677177L, Orientation.FORWARD, "NT_455866.1",
                                          1766472L, 1766472L, Orientation.FORWARD, LocusType.SNP, "4", 91223961L,
                                          91223961L, "T", "T", "A", "T/A", "NC_006091.4:g.91223961T>A", 91223961L,
                                          91223961L, Orientation.FORWARD, "NT_455866.1:g.1766472T>A", 1766472L,
                                          1766472L, Orientation.FORWARD));
        matchingAllelesVariants
                .add(new SubSnpCoreFields(26954817L, Orientation.REVERSE, 13677177L, Orientation.FORWARD, "NT_455866.1",
                                          1766472L, 1766472L, Orientation.FORWARD, LocusType.SNP, "4", 91223961L,
                                          91223961L, "T", "T", "C", "G/A", "NC_006091.4:g.91223961T>C", 91223961L,
                                          91223961L, Orientation.FORWARD, "NT_455866.1:g.1766472T>C", 1766472L,
                                          1766472L, Orientation.FORWARD));
        matchingAllelesVariants
                .add(new SubSnpCoreFields(26963037L, Orientation.FORWARD, 13677177L, Orientation.FORWARD, "NT_455866.1",
                                          1766472L, 1766472L, Orientation.FORWARD, LocusType.SNP, "4", 91223961L,
                                          91223961L, "T", "T", "A", "T/A", "NC_006091.4:g.91223961T>A", 91223961L,
                                          91223961L, Orientation.FORWARD, "NT_455866.1:g.1766472T>A", 1766472L,
                                          1766472L, Orientation.FORWARD));
        matchingAllelesVariants.add(new SubSnpCoreFields(0, Orientation.REVERSE, 0L, Orientation.FORWARD, "", 0L, 0L,
                                                         Orientation.FORWARD, LocusType.SNP, "4", 0L, 0L, "T", "T", "C",
                                                         "T/A/G", "", 0L, 0L, Orientation.FORWARD, "", 0L, 0L,
                                                         Orientation.FORWARD));
        matchingAllelesVariants.add(new SubSnpCoreFields(0, Orientation.REVERSE, 0L, Orientation.FORWARD, "", 0L, 0L,
                                                         Orientation.FORWARD, LocusType.SNP, "4", 0L, 0L, "AT", "AT",
                                                         "TGG", "TT/AT/CCA", "", 0L, 0L, Orientation.FORWARD, "", 0L,
                                                         0L, Orientation.FORWARD));

        mismatchingAllelesVariants = new ArrayList<>();
        mismatchingAllelesVariants
                .add(new SubSnpCoreFields(26201546, Orientation.FORWARD, 13677177L, Orientation.FORWARD, "NT_455866.1",
                                          1766472L, 1766472L, Orientation.FORWARD, LocusType.SNP, "4", 91223961L,
                                          91223961L, "T", "T", "C", "T/A", "NC_006091.4:g.91223961T>C", 91223961L,
                                          91223961L, Orientation.FORWARD, "NT_455866.1:g.1766472T>C", 1766472L,
                                          1766472L, Orientation.FORWARD));
        mismatchingAllelesVariants
                .add(new SubSnpCoreFields(26954817, Orientation.REVERSE, 13677177L, Orientation.FORWARD, "NT_455866.1",
                                          1766472L, 1766472L, Orientation.FORWARD, LocusType.SNP, "4", 91223961L,
                                          91223961L, "T", "T", "A", "G/A", "NC_006091.4:g.91223961T>A", 91223961L,
                                          91223961L, Orientation.FORWARD, "NT_455866.1:g.1766472T>A", 1766472L,
                                          1766472L, Orientation.FORWARD));
        mismatchingAllelesVariants
                .add(new SubSnpCoreFields(26963037, Orientation.FORWARD, 13677177L, Orientation.FORWARD, "NT_455866.1",
                                          1766472L, 1766472L, Orientation.FORWARD, LocusType.SNP, "4", 91223961L,
                                          91223961L, "T", "T", "C", "T/A", "NC_006091.4:g.91223961T>C", 91223961L,
                                          91223961L, Orientation.FORWARD, "NT_455866.1:g.1766472T>C", 1766472L,
                                          1766472L, Orientation.FORWARD));
        mismatchingAllelesVariants.add(new SubSnpCoreFields(0, Orientation.FORWARD, 0L, Orientation.FORWARD, "", 0L, 0L,
                                                            Orientation.FORWARD, LocusType.SNP, "4", 0L, 0L, "T", "T",
                                                            "C", "T/A/G", "", 0L, 0L, Orientation.FORWARD, "", 0L, 0L,
                                                            Orientation.FORWARD));
    }

    @Test
    public void removeMismatchingVariant() throws Exception {
        assertNull(filter.process(mismatchingAllelesVariants.get(0)));
    }

    @Test
    public void keepMatchingVariant() throws Exception {
        assertNotNull(filter.process(matchingAllelesVariants.get(0)));
    }

    @Test
    public void mismatchingVariantsMustBeRemoved() throws Exception {
        List<SubSnpCoreFields> mixedVariants = new ArrayList<>();
        mixedVariants.addAll(mismatchingAllelesVariants);
        mixedVariants.addAll(matchingAllelesVariants);
        Collections.shuffle(mixedVariants);

        List<SubSnpCoreFields> filteredVariants = new ArrayList<>();
        for (SubSnpCoreFields subSnp : mixedVariants) {
            if (filter.process(subSnp) != null) {
                filteredVariants.add(subSnp);
            }
        }

        isSubSet(filteredVariants, matchingAllelesVariants);
        isSubSet(matchingAllelesVariants, filteredVariants);
    }

    private void isSubSet(Collection<SubSnpCoreFields> subset, Collection<SubSnpCoreFields> superset) {
        for (SubSnpCoreFields subsetElement : subset) {
            assertContains(superset, subsetElement);
        }
    }
}
