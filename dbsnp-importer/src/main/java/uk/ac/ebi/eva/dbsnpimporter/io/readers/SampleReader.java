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
import org.springframework.jdbc.core.JdbcTemplate;

import uk.ac.ebi.eva.dbsnpimporter.models.Sample;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;


/**
 SET search_path TO dbsnp_chicken_9031;

 -- get one ss_id from an assembly and a batch
 SELECT
    subind.subsnp_id
 FROM
    subind
    JOIN batch on subind.batch_id = batch.batch_id
    JOIN subsnp sub ON subind.subsnp_id = sub.subsnp_id
    JOIN snpsubsnplink link ON sub.subsnp_id = link.subsnp_id
    JOIN b150_snpcontigloc loc on loc.snp_id = link.snp_id
    JOIN b150_contiginfo ctg ON ctg.contig_gi = loc.ctg_id
 WHERE
    batch.batch_id = $batch_id
    AND ctg.group_label LIKE $group_label
    AND ctg.group_term IN $group_term
 LIMIT 1;


 -- get the individuals from the assembly and batch
 SELECT
    batch.handle,
    batch.batch_id,
    batch.loc_batch_id,
    indiv.loc_ind_id_upp as individual_name,
    population.loc_pop_id as population,
    indiv.ind_id
    subind.submitted_ind_id
    ped.pa_ind_id
    ped.ma_ind_id
    ped.sex
 FROM
    subind
    JOIN submittedindividual indiv on indiv.submitted_ind_id = subind.submitted_ind_id
    JOIN pedigreeindividual ped on indiv.ind_id = ped.ind_id
    JOIN batch on subind.batch_id = batch.batch_id
    JOIN population on population.pop_id = indiv.pop_id
    JOIN subsnp sub ON subind.subsnp_id = sub.subsnp_id
    JOIN snpsubsnplink link ON sub.subsnp_id = link.subsnp_id
    JOIN b150_snpcontigloc loc on loc.snp_id = link.snp_id
    JOIN b150_contiginfo ctg ON ctg.contig_gi = loc.ctg_id
 WHERE
    sub.subsnp_id = $ss_id
    AND batch.batch_id = $batch_id
    AND ctg.group_label = $group_label
    AND ctg.group_term = $group_term
 -- LIMIT 5
 ;
 */
public class SampleReader extends JdbcPagingItemReader<Sample> {

    public SampleReader(String dbsnpBuild, int batch, String assembly, List<String> assemblyTypes,
                        DataSource dataSource, int pageSize) throws Exception {
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must be greater than zero");
        }

        setDataSource(dataSource);
        setQueryProvider(createQueryProvider(dataSource, dbsnpBuild));
        setParameterValues(getParametersMap(dbsnpBuild, batch, assembly, assemblyTypes, dataSource));
        setRowMapper(new SampleRowMapper());
        setPageSize(pageSize);
    }

    private PagingQueryProvider createQueryProvider(DataSource dataSource, String dbsnpBuild) throws Exception {
        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setSelectClause(
                "SELECT " +
                        "batch.handle AS " + SampleRowMapper.HANDLE +
                        ", batch.batch_id AS " + SampleRowMapper.BATCH_ID +
                        ", batch.loc_batch_id AS " + SampleRowMapper.BATCH_NAME +
                        ", indiv.loc_ind_id_upp AS " + SampleRowMapper.INDIVIDUAL_NAME +
                        ", population.loc_pop_id AS " + SampleRowMapper.POPULATION +
                        ", indiv.ind_id AS " + SampleRowMapper.INDIVIDUAL_ID +
                        ", subind.submitted_ind_id AS " + SampleRowMapper.SUBMITTED_INDIVIDUAL_ID +
                        ", ped.pa_ind_id AS " + SampleRowMapper.FATHER_ID +
                        ", ped.ma_ind_id AS " + SampleRowMapper.MOTHER_ID +
                        ", ped.sex AS " + SampleRowMapper.SEX
        );
        factoryBean.setFromClause(
                "FROM " +
                        "subind " +
                        "JOIN submittedindividual indiv on indiv.submitted_ind_id = subind.submitted_ind_id " +
                        "JOIN pedigreeindividual ped on indiv.ind_id = ped.ind_id " +
                        "JOIN batch on subind.batch_id = batch.batch_id " +
                        "JOIN population on population.pop_id = indiv.pop_id " +
                        "JOIN subsnp sub ON subind.subsnp_id = sub.subsnp_id " +
                        "JOIN snpsubsnplink link ON sub.subsnp_id = link.subsnp_id " +
                        "JOIN b" + dbsnpBuild + "_snpcontigloc loc on loc.snp_id = link.snp_id " +
                        "JOIN b" + dbsnpBuild + "_contiginfo ctg ON ctg.contig_gi = loc.ctg_id "
        );
        factoryBean.setWhereClause(
                "WHERE " +
                        "sub.subsnp_id = :ss_id " +
                        "AND batch.batch_id = :batch " +
                        "AND ctg.group_term IN (:assemblyType) " +
                        "AND ctg.group_label LIKE :assembly "
        );
        factoryBean.setSortKey(SampleRowMapper.SUBMITTED_INDIVIDUAL_ID);

        return factoryBean.getObject();
    }

    private Map<String, Object> getParametersMap(String dbsnpBuild, int batch, String assembly,
                                                 List<String> assemblyTypes,
                                                 DataSource dataSource) throws SQLException {
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("assemblyType", assemblyTypes);
        parameterValues.put("assembly", assembly);
        parameterValues.put("ss_id", getSubsnpId(dbsnpBuild, batch, assembly, assemblyTypes, dataSource));
        parameterValues.put("batch", batch);
        return parameterValues;
    }

    private Long getSubsnpId(String dbsnpBuild, int batch, String assembly, List<String> assemblyTypes,
                             DataSource dataSource) throws SQLException {
        String joinedAssemblyTypes = assemblyTypes.stream().map(this::quote).collect(Collectors.joining(","));

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        Long subsnp_id = jdbcTemplate.query(
                "SELECT " +
                        "    sub.subsnp_id " +
                        "  FROM " +
                        "    subind" +
                        "    JOIN batch on subind.batch_id = batch.batch_id" +
                        "    JOIN subsnp sub ON subind.subsnp_id = sub.subsnp_id" +
                        "    JOIN snpsubsnplink link ON sub.subsnp_id = link.subsnp_id" +
                        "    JOIN b" + dbsnpBuild + "_snpcontigloc loc on loc.snp_id = link.snp_id" +
                        "    JOIN b" + dbsnpBuild + "_contiginfo ctg ON ctg.contig_gi = loc.ctg_id" +
                        "  WHERE " +
                        "    batch.batch_id = " + quote(String.valueOf(batch)) +
                        "    AND ctg.group_label LIKE " + quote(assembly) +
                        "    AND ctg.group_term IN (" + joinedAssemblyTypes + ") " +
                        "  LIMIT 1",
                (ResultSet resultSet) -> {
                    if (!resultSet.next()) {
                        throw new NoSuchElementException(
                                "Can't filter samples: no SubSNP found for batch " + batch + ", assembly " + assembly
                                        + ", and assemblyTypes [" + joinedAssemblyTypes + "]");
                    }

                    return resultSet.getObject("subsnp_id", Long.class);
                });

        return subsnp_id;
    }

    private String quote(String s) {
        return "'" + s + "'";
    }
}
