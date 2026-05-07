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
package com.webxells.dis.api.config;

import com.webxells.dis.api.Dataset;
import com.webxells.dis.api.MappingOperation;
import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.validator.Validator;
import java.util.List;
import java.util.Optional;

public interface MappingPart extends ConfigurableByType {
    enum MultiToSingleSelectStrategy {
        FIRST, LAST, ERROR
    }

    MappingPoint getInput();
    void setInput(MappingPoint mappingPoint);
    MappingPoint getOutput();
    void setOutput(MappingPoint mappingPoint);

    default MappingPoint getMappingPointBySource(final MappingPortrayal.Source source) {
        return source == MappingPortrayal.Source.INPUT ? getInput() : getOutput();
    }

    Dataset getDataset();
    Optional<String> value();
    default boolean isStable() {
        return false;
    }

    MappingConfiguration getConfiguration();

    List<MappingOperation> getOperations();
    void setOperations(List<MappingOperation> operations);

    Validator.ErrorStrategy getValidatorErrorStrategy();
    void setValidatorErrorStrategy(Validator.ErrorStrategy errorStrategy);

    MultiToSingleSelectStrategy getMultiToSingleSelectStrategy();
    void setMultiToSingleSelectStrategy(MultiToSingleSelectStrategy multiToSingleSelectStrategy);

    List<MappingConfiguration> getSubData();

    boolean hasRefinement(Class<? extends Refinement> clazz);
    <T extends Refinement> Optional<T> getFirstRefinement(Class<T> clazz);
    <T extends Refinement> List<T> getAllRefinements(Class<T> clazz);
    List<Refinement> getRefinements();

    void clear();

    /**
     * Deprecated!
     *  You should not set multiple subData by config
     */
    @Deprecated
    void setSubData(List<MappingConfiguration> subData);
    void setSubData(MappingConfiguration subData);
    void addSubData(MappingConfiguration subData);
    void addSubData(List<MappingConfiguration> subData);

    void addAllRefinements(List<Refinement> refinements);
    void addRefinement(Refinement refinement);

    MappingPart copy();
    MappingPart copy(MappingConfiguration configuration);

    void copyValues(MappingPart other);
}