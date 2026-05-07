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
package com.webxells.dis.csv;

import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.csv.internal.CsvConfiguration;

@Description("Reads csv data")
public class CsvInputConfig extends CsvConfig implements InputConfig {

    @Description("Source of the data")
    @Required
    private Resource receiver;
    @Description("""
            What to do when ever an unknown column is encountered:
              CREATE = new temporary MappingPart is created,
              IGNORE = skip column,
              ERROR  = raise an error""")
    @Default("IGNORE")
    private UnknownColumnStrategy unknownColumn = UnknownColumnStrategy.IGNORE;
    @Description("""
            What to do when ever an already known column is encountered:
              MERGE  = create multiple datasets of first MappingPart with this source,
              FIND_CORRESPONDENT = first column goes to first MappingPart, second column to second MappingPart and so on
              CREATE = new temporary MappingPart is created,
              IGNORE = skip column,
              ERROR  = raise an error""")
    @Default("IGNORE")
    private SameColumnStrategy sameColumn = SameColumnStrategy.IGNORE;

    @Description("Ignores lines with no data \n-> rows with all empty values are not treated as empty lines!")
    @Default("true")
    private boolean ignoringEmptyLines = true;

    public CsvInputConfig() {}

    public CsvInputConfig(final String name, final Resource receiver) {
        this.name = name;
        this.receiver = receiver;
    }

    @Override
    public String getType() {
        return CsvInput.class.getName();
    }

    public Resource getReceiver() {
        return receiver;
    }

    public void setReceiver(final Resource resource) {
        receiver = resource;
    }



    public UnknownColumnStrategy getUnknownColumn() {
        return unknownColumn;
    }

    public void setUnknownColumn(final UnknownColumnStrategy unknownColumn) {
        this.unknownColumn = unknownColumn;
    }

    public SameColumnStrategy getSameColumn() {
        return sameColumn;
    }

    public void setSameColumn(final SameColumnStrategy sameColumn) {
        this.sameColumn = sameColumn;
    }

    public boolean isIgnoringEmptyLines() {
        return ignoringEmptyLines;
    }

    public void setIgnoringEmptyLines(final boolean ignoringEmptyLines) {
        this.ignoringEmptyLines = ignoringEmptyLines;
    }
}