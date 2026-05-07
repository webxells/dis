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

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.info.internal.PartHandler;
import com.webxells.dis.info.internal.config.RootConfiguration;
import java.util.Optional;

public abstract class SinglePart<T> extends PartHandler implements TypeTransformer<T> {

    protected SinglePart(final RootConfiguration.Configuration configuration) {
        super(configuration);
    }

    protected abstract String getValue(final T current);

    @Override
    public int transform(final T current, final MappingConfiguration configuration) {
        return Optional.ofNullable(getValue(current))
                .map(value -> getMappingParts(configuration, path()).stream()
                            .mapToInt(part -> {
                                saveValue(part, value);
                                return 1;
                            })
                            .sum())
                .orElse(0);
    }

    protected void saveValue(final MappingPart part, final String value) {
        part.getDataset().collect(new SimpleDatasetPiece(value));
    }
}