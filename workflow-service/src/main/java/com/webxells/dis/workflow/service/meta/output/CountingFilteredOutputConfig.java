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
package com.webxells.dis.workflow.service.meta.output;

import com.webxells.dis.base.output.FilteredOutputConfig;
import java.util.ArrayList;
import java.util.List;

public class CountingFilteredOutputConfig extends FilteredOutputConfig {
    public static class CountingFilterEntry extends FilterEntry {
        public String aliasName;
    }

    private List<CountingFilterEntry> countingFilterEntries;

    @Override
    public List<FilterEntry> getFilterEntries() {
        return new ArrayList<>(countingFilterEntries);
    }

    public void setCountingFilterEntries(final List<CountingFilterEntry> countingFilterEntries) {
        this.countingFilterEntries = countingFilterEntries;
    }

    @Override
    public String getType() {
        return CountingFilteredOutput.class.getName();
    }
}