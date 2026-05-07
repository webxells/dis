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
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.info.internal.config.RootConfiguration;
import java.util.Arrays;
import java.util.List;

public abstract class SinglePartKnownMultiValue<T> extends SinglePart<T> {
    private final List<DatasetPiece> values;

    protected SinglePartKnownMultiValue(final String[] values, final RootConfiguration.Configuration configuration) {
        super(configuration);
        this.values = Arrays.stream(values)
                .filter(a -> !a.isBlank())
                .map(value -> (DatasetPiece) new SimpleDatasetPiece(value))
                .toList();
    }

    @Override
    protected String getValue(final T current) {
        return values.isEmpty() ? null : "true";
    }

    @Override
    protected void saveValue(final MappingPart part, final String value) {
        part.getDataset().collect(values);
    }
}