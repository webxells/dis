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
package com.webxells.dis.rest.content;

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.OutputConfig;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.output.Output;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.base.resource.StringResource;
import com.webxells.dis.boot.ServiceManager;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class DatasetTo extends AlteringContentStrategy {
    private static final Logger LOGGER = LoggerProxyFactory.logger(DatasetTo.class);

    @Required
    @Description("Used to get new value")
    protected OutputConfig outputConfig;
    protected Output<?> output;
    protected final String instanceReference = String.valueOf(System.identityHashCode(this));
    protected MappingConfiguration rootDataset;
    @Required(or = {"mappingFilter, aliasFilter"})
    @Description("Uses mapping parts with provided reference for child Output")
    protected String referenceMask;
    @Required(or = {"referenceMask", "aliasFilter"})
    @Description("Uses specific mapping parts for child Output")
    protected List<MappingPortrayal> mappingFilter;
    @Required(or = {"referenceMask", "mappingFilter"})
    @Description("Uses specific mapping parts with new name for child Output")
    protected Map<String, MappingPortrayal> aliasFilter;
    @Description("Uses whole MappingConfiguration for child Output")
    @Default("false")
    protected boolean skipReferenceVerification;

    @Override
    public void validate() throws InvalidApi {
        if (null == referenceMask && !skipReferenceVerification && null == mappingFilter && null == aliasFilter) {
            throw new InvalidApi("reference definition missing");
        }
        if (null == outputConfig) {
            throw new InvalidApi("Output config missing");
        }
        initialize();
    }

    @Override
    public void setContent(final Object content) {
        if (content instanceof MappingConfiguration) {
            rootDataset = (MappingConfiguration) content;
        }
    }

    public void setOutputConfig(final OutputConfig outputConfig) {
        this.outputConfig = outputConfig;
    }

    public void setReferenceMask(final String referenceMask) {
        this.referenceMask = referenceMask;
    }

    public void setMappingFilter(final List<MappingPortrayal> mappingFilter) {
        this.mappingFilter = mappingFilter;
    }

    public void setAliasFilter(final Map<String, MappingPortrayal> aliasFilter) {
        this.aliasFilter = aliasFilter;
    }

    public void setSkipReferenceVerification(final boolean skipReferenceVerification) {
        this.skipReferenceVerification = skipReferenceVerification;
    }

    protected String getGeneratedOutput() {
        try {
            initialize();
            write();
            final ByteArrayOutputStream arrayOutputStream =
                    (ByteArrayOutputStream) ServiceManager.extractResourceOfConfig(outputConfig).send();
            final String result = arrayOutputStream.toString();
            arrayOutputStream.reset();
            LOGGER.trace(String.format("Data appending to next rest call%n%s", result));
            return result;
        } catch (final DisException e) {
            throw new RuntimeException("Error while generate dataset output", e);
        }
    }

    private void write() throws DisException {
        output.start();
        output.write(getDataset());
        output.end();
    }

    protected MappingConfiguration getDataset() {
        if (skipReferenceVerification) {
            return createSkipReferenceVerificationConfig();
        }
        if (null != mappingFilter || null != aliasFilter) {
            final SimpleMappingConfiguration configuration = new SimpleMappingConfiguration();
            if (null != mappingFilter) {
                createFilteredConfig(configuration);
            }
            if (null != aliasFilter) {
                 createAliasConfig(configuration);
            }
            return configuration;
        }
        return rootDataset;
    }

    protected void createAliasConfig(final SimpleMappingConfiguration configuration) {
        for (final Map.Entry<String, MappingPortrayal> current : aliasFilter.entrySet()) {
            rootDataset.getByPortrayal(current.getValue())
                    .ifPresent(mappingPart -> configuration.addPart(new SimpleMappingPart(configuration,
                            new SimpleMappingPoint(instanceReference, current.getKey()),
                            new SimpleMappingPoint(instanceReference, current.getKey())) {{
                        getDataset().collect(mappingPart.getDataset().getContent());
                        addSubData(mappingPart.getSubData());
                    }}));
        }
    }



    protected void createFilteredConfig(final SimpleMappingConfiguration configuration) {
        configuration.setParts(mappingFilter.stream()
                .map(a -> rootDataset.getByPortrayal(a))
                .filter(Optional::isPresent)
                .map(a -> createFakeMappingPart(configuration, a.get()))
                .collect(Collectors.toList())
        );
    }

    protected MappingPart createFakeMappingPart(final SimpleMappingConfiguration configuration, final MappingPart old) {
        return new SimpleMappingPart(configuration, new SimpleMappingPoint(instanceReference,
                Objects.requireNonNullElse(old.getInput(), old.getOutput()).getPath()),
                new SimpleMappingPoint(instanceReference, Objects.requireNonNullElse(old.getOutput(), old.getInput()).getPath())) {{
            getDataset().collect(old.getDataset().getContent());
            addSubData(old.getSubData());
        }};
    }

    protected MappingConfiguration createSkipReferenceVerificationConfig() {
        final SimpleMappingConfiguration configuration = new SimpleMappingConfiguration();
        configuration.setParts(rootDataset.parts().stream()
                .map(old -> createFakeMappingPart(configuration, old))
                .collect(Collectors.toList())
        );
        return configuration;
    }

    private void initialize() throws InvalidApi {
        if (null == output) {
            try {
                setStringResource();
                setName();
                output = ServiceManager.loadByConfig(outputConfig);
            } catch (final NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new InvalidApi("Illegal output config without sender or name provided");
            }
        }
    }

    private void setStringResource() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Method setSender = outputConfig.getClass().getMethod("setSender", Resource.class);
        setSender.invoke(outputConfig, new StringResource());
    }

    private void setName() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Method setName = outputConfig.getClass().getMethod("setName", String.class);
        setName.invoke(outputConfig, skipReferenceVerification || null == referenceMask ? instanceReference :
                referenceMask);
    }


}