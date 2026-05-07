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
package com.webxells.dis.fileregistry.input.linker;

import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;

@Description("Like HashFileRegistry - But hash is saved of mapping part of source")
public class HashFileRegistryWithHashField extends HashFileRegistry {
    @Description("Where to save/look up hash")
    @Required
    private MappingPortrayal hashField;
    private String lastHash;

    @Override
    public void validate() throws InvalidApi {
        super.validate();
        if (null == hashField) {
            throw new InvalidApi("hashField is required");
        }
    }

    @Override
    public int getData(final MappingConfiguration from) throws InputOutputError {
        final MappingConfiguration strippedConfiguration = stripConfiguration(from);
        lastHash = createDataHash(strippedConfiguration);
        addHashField(strippedConfiguration, lastHash);
        return callFileRegistry(lastHash, strippedConfiguration, from);
    }

    @Override
    public String getType() {
        return HashFileRegistryWithHashField.class.getName();
    }

    public void setHashField(final MappingPortrayal hashField) {
        this.hashField = hashField;
    }

    private void addHashField(final MappingConfiguration strippedConfiguration, final String hash) {
        strippedConfiguration.parts().add(new SimpleMappingPart(strippedConfiguration,
                new SimpleMappingPoint(hashField.getReference(), hashField.getPath()),
                new SimpleMappingPoint(hashField.getReference(), hashField.getPath())) {{
            getDataset().collect(new SimpleDatasetPiece(hash));
        }});
    }

    public String getLastHash() {
        return lastHash;
    }
}