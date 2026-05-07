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
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.officex.ReaderConfig;

public abstract class OfficeXssfReaderConfig extends ReaderConfig {
    public enum CellAccessType {
        @Description("Uses column indexes as paths") INDEX_COUNT,
        @Description("Uses excel column references as paths") EXCEL_REFERENCE,
        @Description("Uses values of the first line as paths") VALUE_OF_FIRST_LINE
    }

    @Description("Skips this amount first lines")
    @Default("0")
    private int skipLines;
    @Description("Where to receive the data from")
    private Resource receiver;
    @Description("How to address the cells in the mapping part paths")
    @Default("INDEX_COUNT")
    private CellAccessType cellAccessType = CellAccessType.INDEX_COUNT;
    @Description("Ignores empty rows")
    @Default("true")
    private boolean skipEmptyRows = true;
    @Description("If reader encounters multiple columns with same name")
    @Default("FIRST")
    private MultiValueStrategy multiValueStrategy = MultiValueStrategy.FIRST;

    public CellAccessType getCellAccessType() {
        return cellAccessType;
    }

    public void setCellAccessType(final CellAccessType cellAccessType) {
        this.cellAccessType = cellAccessType;
    }

    public int getSkipLines() {
        return skipLines;
    }

    public void setSkipLines(final int skipLines) {
        this.skipLines = skipLines;
    }

    public Resource getReceiver() {
        return receiver;
    }

    public void setReceiver(final Resource receiver) {
        this.receiver = receiver;
    }

    public boolean isSkipEmptyRows() {
        return skipEmptyRows;
    }

    public void setSkipEmptyRows(final boolean skipEmptyRows) {
        this.skipEmptyRows = skipEmptyRows;
    }

    public MultiValueStrategy getMultiValueStrategy() {
        return multiValueStrategy;
    }

    public void setMultiValueStrategy(final MultiValueStrategy multiValueStrategy) {
        this.multiValueStrategy = multiValueStrategy;
    }
}