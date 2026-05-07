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
package com.webxells.dis.officex.xlsx;

import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.officex.xlsx.selector.All;
import com.webxells.dis.officex.xlsx.selector.SheetSelector;


@Description("Input to read multiple excel sheets")
public class XssfMultiSheetInputConfig extends OfficeXssfReaderConfig {
    @Description("Defines which sheets to read")
    @Default("com.webxells.dis.handler.excel.selector.All")
    private SheetSelector selector = All.INSTANCE;

    @Override
    public String getType() {
        return XssfMultiSheetInput.class.getName();
    }

    public SheetSelector getSelector() {
        return selector;
    }

    public void setSelector(final SheetSelector selector) {
        this.selector = selector;
    }
}