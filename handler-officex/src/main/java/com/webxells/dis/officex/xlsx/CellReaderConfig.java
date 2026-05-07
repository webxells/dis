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

@Description("Input to read a single excel sheet")
public class CellReaderConfig extends OfficeXssfReaderConfig {
    @Description("Sheet name of the excel file")
    private String sheetName;
    @Description("If worksheets have the same name you may define which sheet you want to select (e.g. 3 would select the third sheet with the name of sheetName)")
    @Default("1 - the first sheet encountered with sheetName")
    private int sameNameSheetNumber = 1;

    public String getSheetName() {
        return sheetName;
    }

    @Override
    public String getType() {
        return CellReader.class.getName();
    }

    public void setSheetName(final String sheetName) {
        this.sheetName = sheetName;
    }

    public int getSameNameSheetNumber() {
        return sameNameSheetNumber;
    }

    public void setSameNameSheetNumber(final int sameNameSheetNumber) {
        this.sameNameSheetNumber = sameNameSheetNumber;
    }
}