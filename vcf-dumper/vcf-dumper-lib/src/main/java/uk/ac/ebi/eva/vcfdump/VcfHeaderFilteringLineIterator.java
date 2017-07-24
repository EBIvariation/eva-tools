package uk.ac.ebi.eva.vcfdump;

import htsjdk.tribble.readers.LineIteratorImpl;
import htsjdk.tribble.readers.LineReaderUtil;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class VcfHeaderFilteringLineIterator extends LineIteratorImpl {

    private Set<String> fieldsToExclude;

    public VcfHeaderFilteringLineIterator(InputStream inputStream, String ... fieldsToExclude) {
        super(LineReaderUtil.fromBufferedStream(inputStream));
        this.fieldsToExclude =
                Arrays.stream(fieldsToExclude).map(field -> "##" + field + "=").collect(Collectors.toSet());
    }

    /**
     * Return the next header line, excluding the ones starting with any of the fields to filter
     * @return The following not filtered header line
     */
    protected String advance() {
        String line = super.advance();
        while (line != null && excludeLine(line)) {
            line = super.advance();
        }
        return line;
    }

    private boolean excludeLine(String line) {
        return fieldsToExclude.stream().anyMatch(field -> line.startsWith(field));
    }
}
