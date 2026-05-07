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
import com.webxells.dis.rest.url.StandardUriQuery;

@Description("Url en- or decodes value")
public class UrlEncode implements Manipulator {
    public enum Strategy {
        ENCODE, DECODE
    }

    @Default("ENCODE")
    private Strategy strategy = Strategy.ENCODE;
    @Default("UTF-8")
    private String parameterCharset = StandardUriQuery.DEFAULT_PARAMETER_ENCODING;

    @Override
    public void manipulate(final DatasetPiece datasetPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        datasetPiece.value().ifPresent(a -> datasetPiece.rewriteValue(processStrategy(a)));
    }

    private String processStrategy(final String a) {
        if (Strategy.ENCODE == strategy) {
            return StandardUriQuery.encode(a, parameterCharset);
        } else {
            return StandardUriQuery.decode(a, parameterCharset);
        }
    }

    public void setStrategy(final Strategy strategy) {
        this.strategy = strategy;
    }

    public void setParameterCharset(final String parameterCharset) {
        this.parameterCharset = parameterCharset;
    }
}