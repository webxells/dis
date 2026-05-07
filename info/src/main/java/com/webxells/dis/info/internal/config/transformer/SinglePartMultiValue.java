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
package com.webxells.dis.info.internal.config.transformer;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.info.internal.PartHandler;
import com.webxells.dis.info.internal.config.RootConfiguration;
import java.util.List;
import java.util.Optional;

public abstract class SinglePartMultiValue<T> extends PartHandler implements TypeTransformer<T> {

    protected SinglePartMultiValue(final RootConfiguration.Configuration configuration) {
        super(configuration);
    }

    protected abstract List<DatasetPiece> getValues(final T current);

    @Override
    public int transform(final T current, final MappingConfiguration configuration) {
        return Optional.ofNullable(getValues(current))
                .map(values -> getMappingParts(configuration, path()).stream()
                        .mapToInt(part -> {
                            part.getDataset().collect(values);
                            return 1;
                        })
                        .sum())
                .orElse(0);
    }
}