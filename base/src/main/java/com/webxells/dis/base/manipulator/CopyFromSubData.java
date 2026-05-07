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
package com.webxells.dis.base.manipulator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Alias;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;
import com.webxells.dis.base.SimpleDatasetPiece;
import java.util.Optional;

@Description("Copies values from SubData MappingPart into the current one")
public class CopyFromSubData implements SingleCallForAllValuesManipulator {
    @Description("Parent of the SubData MappingPart")
    @Default("Current mapping part")
    private MappingPortrayal root;
    @Required
    @Alias("subData")
    private MappingPortrayal sub;
    @Description("append to current MappingPart")
    @Default("false")
    private boolean append;

    @Override
    public void validate() throws InvalidApi {
        if (null == sub) {
            throw new InvalidApi("sub are required");
        }
    }

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        if (!append) {
            mappingPart.getDataset().clear();
        }
        Optional.ofNullable(root)
                .flatMap(a -> mappingPart.getConfiguration().getByPortrayal(a))
                .orElse(mappingPart).getSubData().stream()
            .flatMap(a -> a.getByPortrayal(sub).stream())
            .flatMap(a -> a.getDataset().getContent().stream())
            .flatMap(a -> a.value().stream())
            .forEach(a -> mappingPart.getDataset().collect(new SimpleDatasetPiece(a)));
    }

    public void setRoot(final MappingPortrayal root) {
        this.root = root;
    }

    public void setSub(final MappingPortrayal sub) {
        this.sub = sub;
    }

    public void setAppend(final boolean append) {
        this.append = append;
    }
}