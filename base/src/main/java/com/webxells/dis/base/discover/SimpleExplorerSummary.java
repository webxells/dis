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
package com.webxells.dis.base.discover;

import com.webxells.dis.api.discover.ExplorerSummary;

public class SimpleExplorerSummary implements ExplorerSummary {
    private final String fileType;
    private long datasets;
    private boolean hasRun;
    private String fileTypeFormat;

    public SimpleExplorerSummary(final String fileType) {
        this.fileType = fileType;
    }

    @Override
    public long getDatasets() {
        return datasets;
    }

    @Override
    public String getFileType() {
        return fileType;
    }

    public void setDatasets(final long datasets) {
        this.datasets = datasets;
    }

    public boolean hasRun() {
        return hasRun;
    }

    public void setHasRun(final boolean hasRun) {
        this.hasRun = hasRun;
    }

    @Override
    public String getFileTypeFormat() {
        return fileTypeFormat;
    }

    public void setFileTypeFormat(final String fileTypeFormat) {
        this.fileTypeFormat = fileTypeFormat;
    }
}