/*
 * Copyright 2020 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.vcfdump.server.configuration;

public class SwaggerParameterDescriptions {

    public static final String STUDY_DESCRIPTION = "Study identifier (separate with comma for multiple studies), " +
            "e.g. PRJEB9799. Individual study identifiers can be looked up in " +
            "https://www.ebi.ac.uk/eva/webservices/rest/v1/meta/studies/all in the field named 'id'.";

    public static final String STUDY_LIST_DESCRIPTION = "Study identifiers, e.g. PRJEB9799. Each individual " +
            "identifier of studies can be looked up in https://www.ebi.ac.uk/eva/webservices/rest/v1/meta/studies/all " +
            "in the field named 'id'.";

    public static final String SPECIES_DESCRIPTION = "First letter of the genus, followed by the full species name, " +
            "e.g. ecaballus_20. Allowed values can be looked up in " +
            "https://www.ebi.ac.uk/eva/webservices/rest/v1/meta/species/list/ " +
            "(use &lt;taxonomyCode&gt__&ltassemblyCode&gt for a given species and assembly).";

    public static final String REGION_DESCRIPTION = "Comma separated genomic regions in the format chr:start-end, " +
            "e.g. 1:3000000-3001000";

    public static final String ANNOTATION_CONSEQUENCE_TYPE_DESCRIPTION = "Retrieve only variants with exactly this " +
            "consequence type (as stated by Ensembl VEP)";

    public static final String MINOR_ALLELE_FREQUENCY_DESCRIPTION = "Minor Allele Frequency comparison criterion, " +
            "e.g. '<0.1', '>=0.1'";

    public static final String POLYPHEN_DESCRIPTION = "PolyPhen score as stated by Ensembl VEP, e.g. ' <0.1'";

    public static final String SIFT_DESCRIPTION = "SIFT score as stated by Ensembl VEP, e.g. '<0.1'";

    public static final String REFERENCE_ALLELE_DESCRIPTION = "Reference allele, e.g. A";

    public static final String ALTERNATE_ALLELE_DESCRIPTION = "Alternate allele, e.g. T";

    public static final String FORMAT_DESCRIPTION = "Format in which the data will be represented, e.g. VCF";

    public static final String REFERENCE_SEQUENCE_NAME_DESCRIPTION = "Reference sequence name, e.g. 1 or chr1 or " +
            "CM000001.1";

    public static final String START_POSITION_DESCRIPTION = "Start position (0-based inclusive), e.g. 3000000";

    public static final String END_POSITION_DESCRIPTION = "End position  (0-based exclusive), e.g. 3010000";
}
