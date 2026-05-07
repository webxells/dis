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
package com.webxells.dis.info.internal.config.transformer.option;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.info.internal.PartWrapper;
import com.webxells.dis.info.internal.clazz.Option;
import com.webxells.dis.info.internal.config.RootConfiguration;
import com.webxells.dis.info.internal.config.transformer.SinglePart;
import java.util.concurrent.atomic.AtomicInteger;

public class RequiredOption extends SinglePart<Option> {
    private final RootConfiguration.Configuration configuration;

    public RequiredOption(final RootConfiguration.Configuration configuration) {
        super(configuration);
        this.configuration = configuration;
    }

    @Override
    protected String getValue(final Option current) {
        return "true";
    }

    @Override
    public String path() {
        return "required";
    }

    @Override
    public int transform(final Option option, final MappingConfiguration mappingConfiguration) {
        final AtomicInteger result = new AtomicInteger();
        final Required annotation = option.getAnnotation(Required.class)
                .map(a -> {
                    result.addAndGet(super.transform(option, mappingConfiguration));
                    return a;
                }).orElse(null);
        if (null != annotation) {
            final RequiredCondition xor = new RequiredCondition(configuration, "xor", annotation.xor());
            final RequiredCondition or = new RequiredCondition(configuration, "or", annotation.or());
            final RequiredCondition ifPresent = new RequiredCondition(configuration, "ifPresent", annotation.ifPresent());

            getMappingParts(mappingConfiguration, "requiredCondition")
                    .forEach(a -> {
                        final PartWrapper part = new PartWrapper(a);
                        final MappingConfiguration firstOrNewSubData = part.getFirstOrNewSubData();
                        result.addAndGet(or.transform(option, firstOrNewSubData));
                        result.addAndGet(xor.transform(option, firstOrNewSubData));
                        result.addAndGet(ifPresent.transform(option, firstOrNewSubData));
                    });
        }
        return result.get();
    }
}