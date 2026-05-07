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
package com.webxells.dis.base.manipulator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.manipulator.Manipulator;
import com.webxells.dis.api.validator.Validator;
import com.webxells.dis.base.manipulator.setter.OverruleSetter;
import java.util.List;
import java.util.Optional;

@Description("Validates data and determines the further behaviour (if-then-else)")
public class Overrule implements Manipulator {
    @Description("List of Validators that all have to be fulfilled")
    private List<Validator> validators;
    @Description("Action if validation was successful")
    private OverruleSetter ifSetter;
    @Description("Action if validation was not successful")
    private OverruleSetter elseSetter;
    @Description("Clears current values if no OverruleSetter is present")
    @Default("false")
    private boolean clearIfLinkerHasNoValue;

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        Optional.ofNullable(getRightSetter(currentPiece, mappingPart))
                .map(a -> a.getValue(currentPiece, mappingPart))
                .ifPresentOrElse(a -> this.overwrite(a, currentPiece, mappingPart), () -> {
                    if (clearIfLinkerHasNoValue) {
                        clearValue(currentPiece, mappingPart);
                    }
                });
    }

    protected void clearValue(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        currentPiece.rewriteValue(null);
    }

    protected void overwrite(final String newValue, final DatasetPiece currentPiece, final MappingPart mappingPart) {
        currentPiece.rewriteValue(newValue);
    }


    private OverruleSetter getRightSetter(final DatasetPiece mappingPiece, final MappingPart mappingPart) {
        if (null == validators || validators.stream().allMatch(b -> b.validate(mappingPiece, mappingPart))) {
            return ifSetter;
        }
        return elseSetter;
    }

    public void setIfSetter(final OverruleSetter ifSetter) {
        this.ifSetter = ifSetter;
    }

    public void setElseSetter(final OverruleSetter elseSetter) {
        this.elseSetter = elseSetter;
    }

    public void setValidators(final List<Validator> validators) {
        this.validators = validators;
    }

    public void setClearIfLinkerHasNoValue(final boolean clearIfLinkerHasNoValue) {
        this.clearIfLinkerHasNoValue = clearIfLinkerHasNoValue;
    }
}