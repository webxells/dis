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
import com.webxells.dis.info.internal.ForEachObject;
import com.webxells.dis.info.internal.PartWrapper;
import com.webxells.dis.info.internal.config.RootConfiguration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SubData<T, R> extends ToConfiguration implements TypeTransformer<T> {
    private final List<TypeTransformer<R>> transformers;

    public SubData(final RootConfiguration.Configuration configuration,
                   final List<TypeTransformer<R>> transformers) {
        super(configuration);
        this.transformers = transformers;
    }

    protected abstract ForEachObject<R> getObjects(final T current);

    @Override
    public int transform(final T currentParent, final MappingConfiguration mappingConfiguration) {
        final AtomicInteger result = new AtomicInteger();
        getMappingParts(mappingConfiguration, path()).stream()
                .map(PartWrapper::new)
                .forEach(mappingPart -> {
                    getObjects(currentParent).forEachImplementation(current -> {
                        final MappingConfiguration subData = mappingPart.createBlankSubData();
                        transformers.forEach(transformer ->
                                result.addAndGet(transformer.transform(current, subData)));
                    });
                });
        return result.get();
    }

    public abstract String path();
}