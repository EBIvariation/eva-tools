package uk.ac.ebi.eva.vcfdump;

import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VcfHeaderFilteringLineIteratorTest {

    public static final int HEADER_LINES = 18;

    private InputStream inputStream;

    @Before
    public void setUp() throws Exception {
        inputStream = this.getClass().getResourceAsStream("/vcfTestHeader.vcf");
    }

    @Test
    public void iteratingWithoutFiltersWillReturnAllLines() {
        VcfHeaderFilteringLineIterator iterator = new VcfHeaderFilteringLineIterator(inputStream);

        assertEquals(HEADER_LINES, countLinesUsingIterator(iterator));
    }

    @Test
    public void iterateExcludingNonExistingFieldsWillReturnAllLines() {
        VcfHeaderFilteringLineIterator iterator = new VcfHeaderFilteringLineIterator(inputStream, "NotExistingField1",
                                                                                     "NotExistingField2");

        assertEquals(HEADER_LINES, countLinesUsingIterator(iterator));
    }

    @Test
    public void iterateOverHeaderExcludingInfoFields() {
        VcfHeaderFilteringLineIterator iterator = new VcfHeaderFilteringLineIterator(inputStream, "INFO");

        assertIteratorReturnHeaderLine(iterator, "##fileformat=VCFv4.2");
        assertIteratorReturnHeaderLine(iterator, "##ALT=<ID=CNV:124,Description=\"Copy number allele: 124 copies\">");
        assertIteratorReturnHeaderLine(iterator, "##ALT=<ID=DEL,Description=\"Deletion\">");
        assertIteratorReturnHeaderLine(iterator, "##FILTER=<ID=PASS,Description=\"All filters passed\">");
        assertIteratorReturnHeaderLine(iterator,
                                       "##FORMAT=<ID=DS,Number=1,Type=Float,Description=\"Genotype dosage from " +
                                               "MaCH/Thunder\">");
        assertIteratorReturnHeaderLine(iterator,
                                       "##FORMAT=<ID=GL,Number=.,Type=Float,Description=\"Genotype Likelihoods\">");
        assertIteratorReturnHeaderLine(iterator, "##FORMAT=<ID=GT,Number=1,Type=String,Description=\"Genotype\">");
        assertIteratorReturnHeaderLine(iterator, "##FORMAT=<ID=PL,Number=G,Type=Integer,Description=\"Normalized, " +
                "Phred-scaled likelihoods for genotypes as defined in the VCF specification\">");
        assertIteratorReturnHeaderLine(iterator, "##contig=<ID=hs37d5,assembly=b37,length=35477943>");
        assertIteratorReturnHeaderLine(iterator, "##fileDate=20150218");
        assertIteratorReturnHeaderLine(iterator, "##reference=GRCh37");
        assertIteratorReturnHeaderLine(iterator, "##source=1000GenomesPhase3Pipeline");
        assertIteratorReturnHeaderLine(iterator,
                                       "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT\tERZX00049_HG03976" +
                                               "\tERZX00049_HG03977\tERZX00049_HG03978\tERZ015361_HG00381\tERZ015361_HG00382\tERZ015361_HG00383");

        assertFalse(iterator.hasNext());

    }

    @Test
    public void iterateOverHeaderExcludingInfoAndFormatFields() {
        VcfHeaderFilteringLineIterator iterator = new VcfHeaderFilteringLineIterator(inputStream, "INFO", "FORMAT");

        assertIteratorReturnHeaderLine(iterator, "##fileformat=VCFv4.2");
        assertIteratorReturnHeaderLine(iterator, "##ALT=<ID=CNV:124,Description=\"Copy number allele: 124 copies\">");
        assertIteratorReturnHeaderLine(iterator, "##ALT=<ID=DEL,Description=\"Deletion\">");
        assertIteratorReturnHeaderLine(iterator, "##FILTER=<ID=PASS,Description=\"All filters passed\">");
        assertIteratorReturnHeaderLine(iterator, "##contig=<ID=hs37d5,assembly=b37,length=35477943>");
        assertIteratorReturnHeaderLine(iterator, "##fileDate=20150218");
        assertIteratorReturnHeaderLine(iterator, "##reference=GRCh37");
        assertIteratorReturnHeaderLine(iterator, "##source=1000GenomesPhase3Pipeline");
        assertIteratorReturnHeaderLine(iterator,
                                       "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT\tERZX00049_HG03976" +
                                               "\tERZX00049_HG03977\tERZX00049_HG03978\tERZ015361_HG00381\tERZ015361_HG00382\tERZ015361_HG00383");

        assertFalse(iterator.hasNext());

    }

    private void assertIteratorReturnHeaderLine(VcfHeaderFilteringLineIterator iterator, String expectedLine) {
        assertTrue(iterator.hasNext());
        String returnedLine = iterator.next();
        assertEquals(expectedLine, returnedLine);
    }

    private int countLinesUsingIterator(Iterator iterator) {
        int lines = 0;
        while (iterator.hasNext()) {
            iterator.next();
            lines++;
        }
        return lines;
    }
}