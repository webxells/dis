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
package com.webxells.dis.sql;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.input.linker.JoinLinker;
import com.webxells.dis.sql.connection.ConnectionStrategy;
import com.webxells.dis.sql.internal.query.LinkedQueryStrategy;

@Description("Linker to read from database by using current values of MappingParts")
public class LinkedQuery implements JoinLinker {
    private Query query;
    private ConnectionStrategy connectionStrategy;
    private String inputName;
    private String outputName;
    private LinkedQueryStrategy linkedQueryStrategy;
    private boolean skipUnusedParameters;

    @Override
    public void validate() throws InvalidApi {
        if (null == linkedQueryStrategy || null == inputName || null == outputName || null == connectionStrategy) {
            throw new InvalidApi("Required fields are missing.");
        }
    }

    @Override
    public int getData(final MappingConfiguration mappingConfiguration) throws InputOutputError {
        linkedQueryStrategy.setContent(mappingConfiguration.partsByDestination(outputName));
        linkedQueryStrategy.setSkipUnusedParameters(skipUnusedParameters);
        try {
            query.start();
        } catch (final DisException e) {
            throw new InputOutputError("Could not start Query reading", e);
        }
        if (query.hasNext()) {
            return query.read(mappingConfiguration);
        }
        return 0;
    }

    @Override
    public String getInputName() {
        return inputName;
    }

    @Override
    public void start() {
        final QueryConfiguration configuration = new QueryConfiguration();
        configuration.setName(inputName);
        configuration.setConnection(connectionStrategy);
        configuration.setQueryStrategy(linkedQueryStrategy);
        query = new Query(configuration);
    }

    @Override
    public void end() throws DisException {
        query.end();
    }

    @Description("Reference of MappingPart.Input to store result")
    @Required
    public void setInputName(final String inputName) {
        this.inputName = inputName;
    }

    @Description("Reference of MappingPart.Output to use for parameters of query")
    @Required
    public void setOutputName(final String outputName) {
        this.outputName = outputName;
    }

    @Required
    public void setSql(final String sql) {
        linkedQueryStrategy = new LinkedQueryStrategy(sql);
    }

    @Description("Connection to use for querying")
    @Required
    public void setConnectionStrategy(final ConnectionStrategy connectionStrategy) {
        this.connectionStrategy = connectionStrategy;
    }

    public void setSkipUnusedParameters(final boolean skipUnusedParameters) {
        this.skipUnusedParameters = skipUnusedParameters;
    }
}