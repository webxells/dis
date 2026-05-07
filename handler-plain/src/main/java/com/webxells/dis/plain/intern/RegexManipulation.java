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
package com.webxells.dis.plain.intern;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.Manipulator;
import com.webxells.dis.plain.intern.RegexOperation;
import java.util.regex.Matcher;

public abstract class RegexManipulation extends RegexOperation implements Manipulator {
    public enum ReplaceType {
        ALL, FIRST
    }

    protected ReplaceType replaceType = ReplaceType.ALL;

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        if (currentPiece.value().isPresent()) {
            currentPiece.rewriteValue(replace(currentPiece.value().get(), mappingPart));
        }
    }

    private String replace(final String string, final MappingPart part) throws InvalidDatasetException {
        final Matcher matcher = getRegex(part.getConfiguration()).matcher(string);

        return compileResult(string, matcher, part);
    }

    protected abstract String compileResult(final String original, final Matcher matcher,
                                            final MappingPart part) throws InvalidDatasetException;

    public void setReplaceType(final ReplaceType replaceType) {
        this.replaceType = replaceType;
    }
}