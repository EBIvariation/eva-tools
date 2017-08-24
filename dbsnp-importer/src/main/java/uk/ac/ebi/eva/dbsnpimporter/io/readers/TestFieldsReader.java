/*
 * Copyright 2017 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.dbsnpimporter.io.readers;

import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO Reads ss (and associated rs ID) coordinates using the following query
     SELECT
         loc.snp_id AS rs_id,
         sub.subsnp_id AS ss_id,
         ctg.contig_acc AS contig_accession,
         ctg.contig_gi AS contig_id,
         loc.lc_ngbr+2 AS contig_start,
         loc.rc_ngbr AS contig_end,
         ctg.contig_chr AS chromosome,
         loc.phys_pos_from + 1 AS chromosome_start,
         loc.phys_pos_from + 1 + loc.asn_to - loc.asn_from AS chromosome_end,
     CASE
        WHEN loc.orientation = 1 THEN -1
        ELSE 1
     END AS snp_orientation,
     CASE
         WHEN ctg.orient = 1 THEN -1
         ELSE 1
     END AS contig_orientation
     FROM
         b148_snpcontigloc loc JOIN
         b148_contiginfo ctg ON ( ctg.ctg_id = loc.ctg_id )
         snpsubsnplink link ON loc.snp_id = link.snp_id JOIN
         subsnp sub ON link.subsnp_id = sub.subsnp_id
     WHERE
         ctg.group_term IN($extract_mappings_for)
         AND ctg.group_label LIKE '$group_label'
     ORDER BY sorting_id ASC;
 */
public class TestFieldsReader extends JdbcPagingItemReader {

    public TestFieldsReader(String assembly, List<String> assemblyType, DataSource dataSource, int pageSize)
            throws Exception {
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must be greater than zero");
        }

        setDataSource(dataSource);
        setQueryProvider(createQueryProvider(dataSource));
        setRowMapper(new SubSnpCoreFieldsRowMapper());
        setPageSize(pageSize);

//        Map<String, String> parameterValues = new HashMap<>();
//        parameterValues.put("assemblyType", String.join(", ", assemblyType));
//        parameterValues.put("assembly", assembly);
//        setParameterValues(parameterValues);
    }

    private PagingQueryProvider createQueryProvider(DataSource dataSource) throws Exception {
        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setSelectClause(
                "SELECT " +
                        "subsnp_id AS ss_id, " +
                        "contig_acc AS contig_accession "
        );
        factoryBean.setFromClause(
                "FROM" +
                        "   testtable"
        );
        factoryBean.setSortKey("ss_id");

        return factoryBean.getObject();
    }
}
