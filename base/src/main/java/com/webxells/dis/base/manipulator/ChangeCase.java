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

@Description("Changes case of string to lower or upper")
public class ChangeCase implements Manipulator {
    public enum Type {
        LOWER, UPPER
    }

    @Default("LOWER")
    private Type toType = Type.LOWER;

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        currentPiece.value()
                .map(this::changeCase)
                .ifPresent(currentPiece::rewriteValue);
    }

    private String changeCase(final String old) {
        switch (toType){
            case LOWER:
                return old.toLowerCase();
            case UPPER:
                return old.toUpperCase();
        }
        throw new UnsupportedOperationException("Invalid type");
    }

    public void setToType(final Type toType) {
        this.toType = toType;
    }

}
