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
package uk.ac.ebi.eva.dbsnpimporter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties
public class Parameters {

    public static final String PROCESSOR = "processor";

    @Field(PROCESSOR)
    private String processor;

    private int dbsnpBuild;

    private String assembly;

    private List<String> assemblyTypes;

    private int pageSize;

    private int batchId;

    private String variantsCollection;

    private int chunkSize;

    public String getProcessor() {
        return processor;
    }

    public void setProcessor(String processor) {
        this.processor = processor;
    }

    public int getDbsnpBuild() {
        return dbsnpBuild;
    }

    public void setDbsnpBuild(int dbsnpBuild) {
        this.dbsnpBuild = dbsnpBuild;
    }

    public String getAssembly() {
        return assembly;
    }

    public void setAssembly(String assembly) {
        this.assembly = assembly;
    }

    public List<String> getAssemblyTypes() {
        return assemblyTypes;
    }

    public void setAssemblyTypes(List<String> assemblyTypes) {
        this.assemblyTypes = assemblyTypes;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getBatchId() {
        return batchId;
    }

    public void setBatchId(int batchId) {
        this.batchId = batchId;
    }

    public String getVariantsCollection() {
        return variantsCollection;
    }

    public void setVariantsCollection(String variantsCollection) {
        this.variantsCollection = variantsCollection;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }
}
