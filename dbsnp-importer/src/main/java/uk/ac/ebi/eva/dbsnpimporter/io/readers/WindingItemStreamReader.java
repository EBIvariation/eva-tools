/*
 * Copyright 2016 EMBL - European Bioinformatics Institute
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

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamReader;

import java.util.List;

/**
 * The winding reader takes a reader that returns an element in each read call and groups them together in a single
 * read call. This class can only be used with {@link ItemStreamReader}.
 *
 * @param <T>
 */
public class WindingItemStreamReader<T> extends WindingItemReader<T> implements ItemStreamReader<List<T>> {

    public WindingItemStreamReader(ItemStreamReader<T> windedReader) {
        super(windedReader);
    }

    @Override
    protected ItemStreamReader<T> getReader() {
        return (ItemStreamReader<T>) super.getReader();
    }

    @Override
    public void close() {
        getReader().close();
    }

    @Override
    public void open(ExecutionContext executionContext) {
        getReader().open(executionContext);
    }

    @Override
    public void update(ExecutionContext executionContext) {
        getReader().update(executionContext);
    }

}