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
package com.webxells.dis.sql.refinement;

import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.base.refinement.SimpleRefinement;

@Description("Refine accessing sql result by index")
public class AccessByIndex extends SimpleRefinement {
    private Integer index;
    private int startIndex;

    public AccessByIndex(final int index) {
        this.index = index;
    }

    public AccessByIndex() { }

    public Integer getIndex() {
        return index;
    }

    @Description("Numeral index")
    @Default("path of MappingPart")
    public void setIndex(final Integer index) {
        this.index = index;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(final int startIndex) {
        this.startIndex = startIndex;
    }
}