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

import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;

import uk.ac.ebi.eva.dbsnpimporter.models.Sample;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;


/**
 SET search_path TO dbsnp_chicken_9031;

 -- get one ss_id from an assembly and a batch
 SELECT
    subind.subsnp_id
 FROM
    subind
    JOIN batch_id_equiv ON batch_id_equiv.subind_batch_id = subind.batch_id
    JOIN batch on batch_id_equiv.subsnp_batch_id = batch.batch_id
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
    batch.handle as handle,
    batch.batch_id as batch_id,
    batch.loc_batch_id_upp as batch_name,
    indiv.loc_ind_id_upp as individual_name,
    population.loc_pop_id_upp as population,
    indiv.ind_id as individual_id,
    subind.submitted_ind_id as submitted_individual_id,
    ped.pa_ind_id as father_id,
    ped.ma_ind_id as mother_id,
    ped.sex as sex
 FROM
    subind
    JOIN submittedindividual indiv on indiv.submitted_ind_id = subind.submitted_ind_id
    LEFT JOIN pedigreeindividual ped on indiv.ind_id = ped.ind_id
    JOIN batch_id_equiv ON batch_id_equiv.subind_batch_id = subind.batch_id
    JOIN batch on batch_id_equiv.subsnp_batch_id = batch.batch_id
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
public class SampleReader extends JdbcCursorItemReader<Sample> {

    private static List<String> assemblyTypes = Arrays.asList("Primary_Assembly", "non-nuclear");

    private int dbsnpBuild;

    private int batch;

    private String assembly;

    private DataSource dataSource;

    public SampleReader(int dbsnpBuild, int batch, String assembly, DataSource dataSource,
                        int pageSize) throws Exception {
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must be greater than zero");
        }

        this.dbsnpBuild = dbsnpBuild;
        this.batch = batch;
        this.assembly = assembly;
        this.dataSource = dataSource;

        setDataSource(dataSource);
        setSql(buildSql());
        setPreparedStatementSetter(buildPreparedStatementSetter());
        setRowMapper(new SampleRowMapper());
        setFetchSize(pageSize);
    }

    @Override
    protected void openCursor(Connection connection) {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to set autocommit=false", e);
        }
        super.openCursor(connection);
    }

    private String buildSql() {
        String sql =
                "SELECT"
                        + "    batch.handle AS " + SampleRowMapper.HANDLE
                        + "   ,batch.batch_id AS " + SampleRowMapper.BATCH_ID
                        + "   ,batch.loc_batch_id_upp AS " + SampleRowMapper.BATCH_NAME
                        + "   ,indiv.loc_ind_id_upp AS " + SampleRowMapper.INDIVIDUAL_NAME
                        + "   ,population.loc_pop_id_upp AS " + SampleRowMapper.POPULATION
                        + "   ,indiv.ind_id AS " + SampleRowMapper.INDIVIDUAL_ID
                        + "   ,subind.submitted_ind_id AS " + SampleRowMapper.SUBMITTED_INDIVIDUAL_ID
                        + "   ,ped.pa_ind_id AS " + SampleRowMapper.FATHER_ID
                        + "   ,ped.ma_ind_id AS " + SampleRowMapper.MOTHER_ID
                        + "   ,ped.sex AS " + SampleRowMapper.SEX
                        + " FROM"
                        + "    subind "
                        + "    JOIN submittedindividual indiv on indiv.submitted_ind_id = subind.submitted_ind_id"
                        + "    LEFT JOIN pedigreeindividual ped on indiv.ind_id = ped.ind_id"
                        + "    JOIN batch_id_equiv ON batch_id_equiv.subind_batch_id = subind.batch_id"
                        + "    JOIN batch on batch_id_equiv.subsnp_batch_id = batch.batch_id"
                        + "    JOIN population on population.pop_id = indiv.pop_id"
                        + "    JOIN subsnp sub ON subind.subsnp_id = sub.subsnp_id"
                        + "    JOIN snpsubsnplink link ON sub.subsnp_id = link.subsnp_id"
                        + "    JOIN b" + dbsnpBuild + "_snpcontigloc loc on loc.snp_id = link.snp_id"
                        + "    JOIN b" + dbsnpBuild + "_contiginfo ctg ON ctg.contig_gi = loc.ctg_id"
                        + " WHERE"
                        + "    sub.subsnp_id = ?"
                        + "    AND batch.batch_id = ?"
                        + "    AND ctg.group_term IN (?, ?)"
                        + "    AND ctg.group_label LIKE ?";
        return sql;
    }

    private PreparedStatementSetter buildPreparedStatementSetter() throws Exception {
        PreparedStatementSetter preparedStatementSetter = new ArgumentPreparedStatementSetter(
                new Object[]{
                        getSubsnpId(),
                        batch,
                        assemblyTypes.get(0),
                        assemblyTypes.get(1),
                        assembly
                }
        );
        return preparedStatementSetter;
    }

    private Long getSubsnpId() throws SQLException {
        String joinedAssemblyTypes = assemblyTypes.stream().map(this::quote).collect(Collectors.joining(","));

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        try {
            Long subsnp_id = jdbcTemplate.query(
                    "SELECT " +
                            "    sub.subsnp_id " +
                            "  FROM " +
                            "    subind" +
                            "    JOIN batch_id_equiv ON batch_id_equiv.subind_batch_id = subind.batch_id" +
                            "    JOIN batch on batch_id_equiv.subsnp_batch_id = batch.batch_id" +
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
        } catch (BadSqlGrammarException e) {
            throw e.getSQLException(); // This is the really useful exception
        }
    }

    private String quote(String s) {
        return "'" + s + "'";
    }
}
