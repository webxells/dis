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
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.info.internal.clazz.Interface;
import com.webxells.dis.info.internal.config.RootConfiguration;

public class InterfaceName extends ToConfiguration {
    private final Interface interfaze;

    public InterfaceName(final Interface interfaze, final RootConfiguration.Configuration configuration) {
        super(configuration);
        this.interfaze = interfaze;
    }

    public int transform(final MappingConfiguration current) {
        return getMappingParts(current, path()).stream()
                .mapToInt(a -> {
                    a.getDataset()
                            .collect(new SimpleDatasetPiece(interfaze.getName()));
                    return 1;
                })
                .sum();
    }

    public String path() {
        return "name";
    }


}