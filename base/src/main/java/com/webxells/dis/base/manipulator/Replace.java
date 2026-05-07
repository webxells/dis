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
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.manipulator.Manipulator;
import java.util.regex.Pattern;

@Description("Replaces substrings in current value")
public class Replace implements Manipulator {
    @Description("Search pattern")
    private String search;
    @Description("Value to replace with")
    private String replace;
    //@Todo: remove - use com.webxells.dis.plain.manipulate.RegexReplace
    @Deprecated
    private boolean useRegularExpression;

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        currentPiece.value()
                .ifPresent(a -> currentPiece.rewriteValue(
                        useRegularExpression ?
                                Pattern.compile(search, //@REFACTOR
                                        Pattern.UNICODE_CASE | Pattern.MULTILINE | Pattern.UNICODE_CHARACTER_CLASS).matcher(a).replaceAll(replace) :
                                a.replace(search, replace)));
    }

    public void setSearch(final String search) {
        this.search = search;
    }

    public void setReplace(final String replace) {
        this.replace = replace;
    }

    public void setUseRegularExpression(final boolean useRegularExpression) {
        this.useRegularExpression = useRegularExpression;
    }
}