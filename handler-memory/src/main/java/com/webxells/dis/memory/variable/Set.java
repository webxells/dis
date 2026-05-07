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
package com.webxells.dis.memory.variable;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.Logger;
import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.Manipulator;
import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.memory.registry.VariableRegistry;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Description("Sets/overwrites a variable")
public class Set implements SingleCallForAllValuesManipulator {
    private static final Logger LOGGER = LoggerProxyFactory.logger(Set.class);

    @Description("Variable name")
    @Required
    private String name;
    @Description("Sets a static value")
    @Required(xor = {"values", "valuePortrayal"})
    @Default("Current MappingPart's dataset")
    private String value;
    @Description("Sets static values")
    @Required(xor = {"value", "valuePortrayal"})
    @Default("Current MappingPart's dataset")
    private List<String> values;
    @Description("Sets values of MappingPart's dataset")
    @Required(xor = {"values", "value"})
    @Default("Current MappingPart's dataset")
    private MappingPortrayal valuePortrayal;
    @Description("Values are preprocessed by this manipulator")
    private Manipulator via;
    @Description("Manipulator 'via' is able to alter real data")
    @Default("false")
    private boolean callWithOriginalMappingPart;
    @Description("If Manipulator fails then the error will be logged but skipped")
    @Default("false")
    private boolean skipManipulatorError;

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        setVariable(Optional.ofNullable(value)
                .map(List::of)
                .orElseGet(() -> Optional.ofNullable(values)
                        .orElseGet(() -> getValuesOfDataset(mappingPart))), mappingPart);
    }

    @Override
    public void validate() throws InvalidApi {
        if (null == name) {
            throw new InvalidApi("name is mandatory");
        }
    }

    private List<String> getValuesOfDataset(final MappingPart mappingPart) {
        if (null == valuePortrayal) {
            return createValueList(mappingPart);
        }
        return createValueList(mappingPart.getConfiguration().getByPortrayal(valuePortrayal)
                .orElse(null));

    }

    private List<String> createValueList(final MappingPart mappingPart) {
        return Optional.ofNullable(mappingPart).stream()
                .flatMap(a -> a.getDataset().getContent().stream())
                .flatMap(a -> a.value().stream())
                .toList();
    }


    private void setVariable(final List<String> values, final MappingPart mappingPart) throws InvalidDatasetException {
        VariableRegistry.set(name, null == via ? values : callManipulatorOnValues(values, mappingPart));
    }

    private List<String> callManipulatorOnValues(final List<String> values, final MappingPart mappingPart) throws InvalidDatasetException {
        final MappingPart part = getViaPart(mappingPart);
        final List<String> realValues = new ArrayList<>(values);
        final boolean firstIsPresent = realValues.size() > 0;
        if (via instanceof SingleCallForAllValuesManipulator || !firstIsPresent) {
            callManipulatorOnce(firstIsPresent, realValues, part);
        } else {
            for (int i = 0; i < realValues.size(); i++) {
                final String currentValue = realValues.get(i);
                realValues.remove(i);
                realValues.add(i, callManipulator(currentValue, mappingPart));
            }
        }
        return realValues;
    }

    private void callManipulatorOnce(final boolean firstIsPresent, final List<String> realValues, final MappingPart part) throws InvalidDatasetException {
        final String firstValue = firstIsPresent ? realValues.getFirst() : null;
        final String result = callManipulator(firstValue, part);
        if (null != result || firstIsPresent) {
            if (firstIsPresent) {
                realValues.removeFirst();
            }
            realValues.addFirst(result);
        }
    }

    private String callManipulator(final String current, final MappingPart part) throws InvalidDatasetException {
        final DatasetPiece datasetPiece = new SimpleDatasetPiece(current);
        try {
            via.manipulate(datasetPiece, part);
        } catch (InvalidDatasetException e) {
            if (skipManipulatorError) {
                LOGGER.e("Skipping manipulator error: " + e.getMessage(), e);
                return null;
            }
            throw new InvalidDatasetException("Via manipulation failed", e);
        }
        return datasetPiece.value()
                .orElse(null);
    }

    private MappingPart getViaPart(final MappingPart mappingPart) {
        return callWithOriginalMappingPart ? mappingPart : mappingPart.copy(new SimpleMappingConfiguration());
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public void setValues(final List<String> values) {
        this.values = values;
    }

    public void setValuePortrayal(final MappingPortrayal valuePortrayal) {
        this.valuePortrayal = valuePortrayal;
    }

    public void setVia(final Manipulator via) {
        this.via = via;
    }

    public void setCallWithOriginalMappingPart(final boolean callWithOriginalMappingPart) {
        this.callWithOriginalMappingPart = callWithOriginalMappingPart;
    }

    public void setSkipManipulatorError(final boolean skipManipulatorError) {
        this.skipManipulatorError = skipManipulatorError;
    }
}