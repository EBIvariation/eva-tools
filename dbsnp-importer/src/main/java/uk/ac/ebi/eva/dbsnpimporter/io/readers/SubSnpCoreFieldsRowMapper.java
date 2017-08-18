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

import org.springframework.jdbc.core.RowMapper;

import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Maps the database fields that correspond to an SS ID along with its contig and (optionally) chromosome coordinates.
 *
 * TODO Add reference allele
 * TODO Add alternate allele
 * TODO Add link between RefSNP and SubSNP?
 */
public class SubSnpCoreFieldsRowMapper implements RowMapper {

    public static final String SNP_ID_COLUMN = "snp_id";

    public static final String CONTIG_ACCESION_COLUMN = "contig_accession";

    public static final String CONTIG_START_COLUMN = "contig_start";

    public static final String CONTIG_END_COLUMN = "contig_end";

    public static final String CHROMOSOME_COLUMN = "chromosome";

    public static final String CHROMOSOME_START_COLUMN = "chromosome_start";

    public static final String CHROMOSOME_END_COLUMN = "chromosome_end";

    public static final String SNP_ORIENTATION_COLUMN = "snp_orientation";

    public static final String CONTIG_ORIENTATION_COLUMN = "contig_orientation";


    @Override
    public Object mapRow(ResultSet resultSet, int rowNum) throws SQLException {

        SubSnpCoreFields subSnpCoreFields = new SubSnpCoreFields(
                resultSet.getInt(SNP_ID_COLUMN),
                resultSet.getInt(SNP_ORIENTATION_COLUMN),
                resultSet.getString(CONTIG_ACCESION_COLUMN),
                resultSet.getInt(CONTIG_START_COLUMN),
                resultSet.getInt(CONTIG_END_COLUMN),
                resultSet.getInt(CONTIG_ORIENTATION_COLUMN),
                resultSet.getString(CHROMOSOME_COLUMN),
                resultSet.getObject(CHROMOSOME_START_COLUMN, Integer.class),
                resultSet.getObject(CHROMOSOME_END_COLUMN, Integer.class)
        );

        return subSnpCoreFields;
    }
}
