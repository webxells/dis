/**
 * Copyright (C) 2020-2026 webXells GmbH
 *
 * This work is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivatives 4.0 International Public License.
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://creativecommons.org/licenses/by-nc-nd/4.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 **/
package com.webxells.dis.memory;

import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.memory.indexer.IndexAltering;
import java.util.List;
import java.util.Set;

@Description("Writes data to memory transaction using an index (MurMur3)")
public class IndexedMemoryOutputConfig extends MemoryOutputConfig {
    @Description("Parts to be used as index")
    @Required
    private Set<String> indexFields;
    private List<IndexAltering> indexAltering;

    public IndexedMemoryOutputConfig() { }

    public IndexedMemoryOutputConfig(final String name, final Set<String> indexFields, final List<IndexAltering> indexAltering) {
        setName(name);
        setIndexFields(indexFields);
        setIndexAltering(indexAltering);
    }

    @Override
    public String getType() {
        return IndexedMemoryOutput.class.getName();
    }

    public Set<String> getIndexFields() {
        return indexFields;
    }

    public void setIndexFields(final Set<String> indexFields) {
        this.indexFields = indexFields;
    }

    public List<IndexAltering> getIndexAltering() {
        return indexAltering;
    }

    public void setIndexAltering(final List<IndexAltering> indexAltering) {
        this.indexAltering = indexAltering;
    }
}