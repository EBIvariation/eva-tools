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
package uk.ac.ebi.eva.dbsnpimporter.parameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@ConfigurationProperties
public class Parameters implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(Parameters.class);

    public static final String PROCESSOR = "processor";

    @Field(PROCESSOR)
    private String processor;

    private int dbsnpBuild;

    private String assembly;

    private List<String> assemblyTypes;

    private int pageSize;

    private int batchId;

    private String variantsCollection;

    private String filesCollection;

    private int chunkSize;

    private String contigMappingUrl;

    private String referenceFastaFile;

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.debug("Properties were set to: {}", this);
    }

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

    public String getFilesCollection() {
        return filesCollection;
    }

    public void setFilesCollection(String filesCollection) {
        this.filesCollection = filesCollection;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public String getContigMappingUrl() {
        return contigMappingUrl;
    }

    public void setContigMappingUrl(String contigMappingUrl) {
        this.contigMappingUrl = contigMappingUrl;
    }

    public String getReferenceFastaFile() {
        return referenceFastaFile;
    }

    public void setReferenceFastaFile(String referenceFastaFile) {
        this.referenceFastaFile = referenceFastaFile;
    }

    @Override
    public String toString() {
        return "Parameters{" +
                "processor='" + processor + '\'' +
                ", dbsnpBuild=" + dbsnpBuild +
                ", assembly='" + assembly + '\'' +
                ", assemblyTypes=" + assemblyTypes +
                ", pageSize=" + pageSize +
                ", batchId=" + batchId +
                ", variantsCollection='" + variantsCollection + '\'' +
                ", filesCollection='" + filesCollection + '\'' +
                ", chunkSize=" + chunkSize +
                ", contigMappingUrl='" + contigMappingUrl + '\'' +
                ", referenceFastaFile='" + referenceFastaFile + '\'' +
                '}';
    }
}
