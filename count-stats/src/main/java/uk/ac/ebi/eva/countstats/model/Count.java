package uk.ac.ebi.eva.countstats.model;

import lombok.Value;

import java.math.BigInteger;

@Value
public class Count {
    private String process;
    private String identifier;
    private String metric;
    private BigInteger count;
}
