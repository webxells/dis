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
package com.webxells.dis.test.example.validator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.validator.Validator;
import java.util.List;
import java.util.Map;

public class TestValidator implements Validator {
    private String parameter;
    private List<Map<String, List<String>>> complicated;

    @Override
    public boolean validate(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        return false;
    }

    @Override
    public String getType() {
        return TestValidator.class.getName();
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(final String parameter) {
        this.parameter = parameter;
    }

    public List<Map<String, List<String>>> getComplicated() {
        return complicated;
    }

    public void setComplicated(final List<Map<String, List<String>>> complicated) {
        this.complicated = complicated;
    }
}