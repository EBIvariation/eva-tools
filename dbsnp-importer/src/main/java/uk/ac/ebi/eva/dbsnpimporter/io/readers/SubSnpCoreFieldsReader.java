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

import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.ac.ebi.eva.dbsnpimporter.io.readers.SubSnpCoreFieldsRowMapper.CHROMOSOME_COLUMN;
import static uk.ac.ebi.eva.dbsnpimporter.io.readers.SubSnpCoreFieldsRowMapper.CHROMOSOME_END_COLUMN;
import static uk.ac.ebi.eva.dbsnpimporter.io.readers.SubSnpCoreFieldsRowMapper.CHROMOSOME_START_COLUMN;
import static uk.ac.ebi.eva.dbsnpimporter.io.readers.SubSnpCoreFieldsRowMapper.CONTIG_END_COLUMN;
import static uk.ac.ebi.eva.dbsnpimporter.io.readers.SubSnpCoreFieldsRowMapper.CONTIG_NAME_COLUMN;
import static uk.ac.ebi.eva.dbsnpimporter.io.readers.SubSnpCoreFieldsRowMapper.CONTIG_ORIENTATION_COLUMN;
import static uk.ac.ebi.eva.dbsnpimporter.io.readers.SubSnpCoreFieldsRowMapper.CONTIG_START_COLUMN;
import static uk.ac.ebi.eva.dbsnpimporter.io.readers.SubSnpCoreFieldsRowMapper.REFSNP_ID_COLUMN;
import static uk.ac.ebi.eva.dbsnpimporter.io.readers.SubSnpCoreFieldsRowMapper.SNP_ORIENTATION_COLUMN;
import static uk.ac.ebi.eva.dbsnpimporter.io.readers.SubSnpCoreFieldsRowMapper.SUBSNP_ID_COLUMN;

/**
     SELECT
         sub.subsnp_id AS ss_id,
         loc.snp_id AS rs_id,
         ctg.contig_name AS contig_name,
         loc.asn_from +1 AS contig_start,
         loc.asn_to +1 as contig_end,
         ctg.contig_chr AS chromosome,
         loc.phys_pos_from + 1 AS chromosome_start,
         loc.phys_pos_from + 1 + loc.asn_to - loc.asn_from AS chromosome_end,
         CASE
            WHEN loc.orientation = 1 THEN -1 ELSE 1
         END AS snp_orientation,
         CASE
            WHEN ctg.orient = 1 THEN -1 ELSE 1
         END AS contig_orientation
     FROM
         b148_snpcontigloc loc JOIN
         b148_contiginfo ctg ON ctg.ctg_id = loc.ctg_id JOIN
         snpsubsnplink link ON loc.snp_id = link.snp_id JOIN
         subsnp sub ON link.subsnp_id = sub.subsnp_id
     WHERE
         ctg.group_term IN($extract_mappings_for)
         AND ctg.group_label LIKE '$group_label'
     ORDER BY ss_id ASC;
 */
public class SubSnpCoreFieldsReader extends JdbcPagingItemReader<SubSnpCoreFields> {

    public SubSnpCoreFieldsReader(String assembly, List<String> assemblyTypes, DataSource dataSource, int pageSize)
            throws Exception {
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must be greater than zero");
        }

        setDataSource(dataSource);
        setQueryProvider(createQueryProvider(dataSource));
        setRowMapper(new SubSnpCoreFieldsRowMapper());
        setPageSize(pageSize);

        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("assemblyType", String.join(", ", assemblyTypes));
        parameterValues.put("assembly", assembly);
        setParameterValues(parameterValues);
    }

    private PagingQueryProvider createQueryProvider(DataSource dataSource) throws Exception {
        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setSelectClause(
                "SELECT " +
                        "sub.subsnp_id AS " + SUBSNP_ID_COLUMN +
                        ",loc.snp_id AS " + REFSNP_ID_COLUMN +
                        ",ctg.contig_name AS " + CONTIG_NAME_COLUMN +
                        ",loc.asn_from +1 AS " + CONTIG_START_COLUMN +
                        ",loc.asn_to +1 AS " + CONTIG_END_COLUMN +
                        ",ctg.contig_chr AS " + CHROMOSOME_COLUMN +
                        ",loc.phys_pos_from + 1 AS " + CHROMOSOME_START_COLUMN +
                        ",loc.phys_pos_from + 1 + loc.asn_to - loc.asn_from AS " + CHROMOSOME_END_COLUMN +
                        ",CASE " +
                        "   WHEN loc.orientation = 1 THEN -1 ELSE 1 " +
                        "END AS " + SNP_ORIENTATION_COLUMN +
                        ",CASE " +
                        "   WHEN ctg.orient = 1 THEN -1 ELSE 1 " +
                        "END AS " + CONTIG_ORIENTATION_COLUMN
        );
        factoryBean.setFromClause(
                "FROM " +
                        "b148_snpcontigloc loc JOIN " +
                        "b148_contiginfo ctg ON ctg.ctg_id = loc.ctg_id JOIN " +
                        "snpsubsnplink link ON loc.snp_id = link.snp_id JOIN " +
                        "subsnp sub ON link.subsnp_id = sub.subsnp_id "
        );
        factoryBean.setWhereClause(
                "WHERE " +
                        "ctg.group_term IN (:assemblyType) AND " +
                        "ctg.group_label LIKE :assembly"
        );
        factoryBean.setSortKey(SUBSNP_ID_COLUMN);

        return factoryBean.getObject();
    }
}
