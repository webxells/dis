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
import com.webxells.dis.api.config.description.RespectsRefinements;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.info.internal.clazz.Implementation;
import com.webxells.dis.info.internal.config.RootConfiguration;
import java.util.List;
import java.util.stream.Stream;

public class Refinements extends SinglePartMultiValue<Implementation> {
    public Refinements(final RootConfiguration.Configuration configuration) {
        super(configuration);
    }

    @Override
    protected List<DatasetPiece> getValues(final Implementation current) {
        return current.getAnnotation(RespectsRefinements.class)
                .map(a -> Stream.of(a.value())
                    .map(b -> (DatasetPiece) new SimpleDatasetPiece(b.getName()))
                    .toList())
                .orElse(null);
    }

    @Override
    public String path() {
        return "refinements";
    }
}