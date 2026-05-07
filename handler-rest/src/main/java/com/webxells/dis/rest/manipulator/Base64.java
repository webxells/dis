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
package com.webxells.dis.rest.manipulator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.Manipulator;

@Description("Base64 en- or decodes value")
public class Base64 implements Manipulator {
    public enum Strategy {
        @Description("Converts string to Base64 string") ENCODE,
        @Description("Converts Base64 string back to its original form") DECODE
    }

    @Default("ENCODE")
    private Strategy strategy = Strategy.ENCODE;
    @Description("Does not add padding of the end of the encoded data")
    @Default("false")
    private boolean withoutPadding;

    @Override
    public void manipulate(final DatasetPiece datasetPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        datasetPiece.value().ifPresent(a -> datasetPiece.rewriteValue(processStrategy(a)));
    }

    private String processStrategy(final String a) {
        if (Strategy.ENCODE == strategy) {
            java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
            if (withoutPadding) {
                encoder = encoder.withoutPadding();
            }
            return encoder.encodeToString(a.getBytes());
        } else {
            return new String(java.util.Base64.getDecoder().decode(a));
        }
    }

    public void setStrategy(final Strategy strategy) {
        this.strategy = strategy;
    }

    public void setWithoutPadding(final boolean withoutPadding) {
        this.withoutPadding = withoutPadding;
    }
}