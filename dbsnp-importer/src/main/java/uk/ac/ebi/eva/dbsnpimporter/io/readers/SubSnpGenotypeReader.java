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

import org.springframework.jdbc.BadSqlGrammarException;
import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpGenotype;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static uk.ac.ebi.eva.dbsnpimporter.io.readers.SubSnpGenotypeRowMapper.BATCH_ID_COLUMN;
import static uk.ac.ebi.eva.dbsnpimporter.io.readers.SubSnpGenotypeRowMapper.STUDY_ID_COLUMN;
import static uk.ac.ebi.eva.dbsnpimporter.io.readers.SubSnpGenotypeRowMapper.SUBSNP_ID_COLUMN;
import static uk.ac.ebi.eva.dbsnpimporter.io.readers.SubSnpGenotypeRowMapper.GENOTYPES_COLUMN;


public class SubSnpGenotypeReader extends JdbcPagingItemReader<SubSnpGenotype> {

    private final int batch;

    public SubSnpGenotypeReader(int batch, DataSource dataSource, int pageSize) throws Exception {
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must be greater than zero");
        }

        this.batch = batch;

        setDataSource(dataSource);
        setQueryProvider(createQueryProvider(dataSource));
        setParameterValues(getParametersMap(batch));
        setRowMapper(new SubSnpGenotypeRowMapper());
        setPageSize(pageSize);
    }

    @Override
    public SubSnpGenotype read() throws Exception {
        try {
            return super.read();
        } catch (BadSqlGrammarException e) {
            throw new SQLException("Could not read SubSNP Genotype for batch: " + batch , e);
        }
    }

    private PagingQueryProvider createQueryProvider(DataSource dataSource) throws Exception {
        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setSelectClause("select " +
                "batch_id as " + BATCH_ID_COLUMN +
                ", batch_id as " + STUDY_ID_COLUMN + //Batch ID serves as both file ID and study ID
                ", subsnp_id as " + SUBSNP_ID_COLUMN +
                ", genotypes_string as " + GENOTYPES_COLUMN);
        factoryBean.setFromClause("from subsnpgenotypes");
        factoryBean.setSortKey(SUBSNP_ID_COLUMN);
        factoryBean.setWhereClause(
                "WHERE batch_id = :batch"
        );
        return factoryBean.getObject();
    }

    private Map<String, Object> getParametersMap(int batch) {
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("batch", batch);
        return parameterValues;
    }
}
