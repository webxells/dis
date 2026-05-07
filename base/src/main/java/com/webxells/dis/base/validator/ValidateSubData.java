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
package com.webxells.dis.base.validator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Alias;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.validator.SingleCallForAllValuesValidator;
import com.webxells.dis.api.validator.Validator;
import com.webxells.dis.base.SimpleDatasetPiece;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Description("Runs validation against provided MappingPart (subDataPortrayal) of subData of root")
public class ValidateSubData extends SimpleValidator implements SingleCallForAllValuesValidator {
    public enum VALIDATION_TYPE {
        @Description("Validates if any subData validates") ANY,
        @Description("Only validates if all subData validate") ALL
    }
    private static class DataPartBridge {
        public MappingPart part;
        public DatasetPiece piece;
    }

    @Description("Parent MappingPart of subData")
    @Default("Current MappingPart")
    private MappingPortrayal root;
    @Required
    @Description("SubData MappingPart to validate")
    private MappingPortrayal subDataPortrayal;
    @Required
    @Alias("validator")
    private Validator validation;
    @Description("Fails validation if MappingPart is not found")
    @Default("false")
    private boolean errorIfMissing;
    @Description("Strategy when to validate")
    @Default("ALL")
    private VALIDATION_TYPE validationType = VALIDATION_TYPE.ALL;

    @Override
    public void validate() throws InvalidApi {
        if (null == subDataPortrayal || null == validation) {
            throw new InvalidApi("missing required fields");
        }
    }

    @Override
    protected boolean validateCurrentPiece(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        final AtomicInteger count = new AtomicInteger();
        final boolean result = calcResult(getRoot(mappingPart).stream()
                .flatMap(a -> a.getSubData().stream())
                .flatMap(a -> a.getByPortrayal(subDataPortrayal).stream())
                .flatMap(this::createDataBridge)
                .peek(a -> count.incrementAndGet()));
        return result && (!errorIfMissing || 0 < count.get());
    }

    private boolean calcResult(final Stream<DataPartBridge> stream) {
        switch(validationType) {
            case ALL:
                return stream.allMatch(a -> validation.validate(a.piece, a.part));
            case ANY:
                return stream.anyMatch(a -> validation.validate(a.piece, a.part));
        }
        throw new UnsupportedOperationException("Unknown validationType: ".concat(validationType.name()));
    }

    private Optional<MappingPart> getRoot(final MappingPart mappingPart) {
        if (null == root) {
            return Optional.of(mappingPart);
        }
        return mappingPart.getConfiguration().getByPortrayal(root);
    }

    private Stream<DataPartBridge> createDataBridge(final MappingPart mappingPart) {
        return getAtLeastOne(mappingPart.getDataset().getContent())
                .map(a -> new DataPartBridge() {{part = mappingPart; piece = a;}});
    }

    private Stream<DatasetPiece> getAtLeastOne(final List<DatasetPiece> content) {
        return content.isEmpty() ?
                Optional.of((DatasetPiece) new SimpleDatasetPiece(null)).stream() :
                content.stream();
    }

    public void setErrorIfMissing(final boolean errorIfMissing) {
        this.errorIfMissing = errorIfMissing;
    }

    public void setValidationType(final VALIDATION_TYPE validationType) {
        this.validationType = validationType;
    }

    public void setRoot(final MappingPortrayal root) {
        this.root = root;
    }

    public void setSubDataPortrayal(final MappingPortrayal subDataPortrayal) {
        this.subDataPortrayal = subDataPortrayal;
    }

    public void setValidation(final Validator validation) {
        this.validation = validation;
    }
}
