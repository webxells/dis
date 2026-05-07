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
package com.webxells.dis.localfile.trigger;

import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.base.trigger.SimpleConcurrentTriggerConfig;

@Description("Watches directories for changes")
public class LocalFileEventConfiguration extends SimpleConcurrentTriggerConfig {
    @Required
    @Description("Path to the directory")
    private String path;
    @Description("If provided, other Features like AddCurrentFile may receive file as well")
    private String index;
    @Description("Filters files to watch")
    private LocalFileEvent.FileFilter fileFilter;
    @Required(or = {"watchForModify","watchForDelete"})
    @Description("Looks for created files")
    @Default("true")
    private boolean watchForCreate = true;
    @Required(or = {"watchForCreate","watchForDelete"})
    @Description("Looks for modified files")
    @Default("true")
    private boolean watchForModify = true;
    @Required(or = {"watchForCreate","watchForModify"})
    @Description("Looks for deleted files")
    @Default("false")
    private boolean watchForDelete = false;
    @Description("Raises an error if an event was lost or discarded")
    @Default("false")
    private boolean errorOnOverflow = false;
    @Description("If other applications are currently working with file, this will wait")
    @Default("false")
    private boolean waitForWritingLocks = false;

    @Override
    public void validate() throws InvalidApi {
        if (null == path) {
            throw new InvalidApi("Path required");
        }
    }

    @Override
    public String getType() {
        return LocalFileEvent.class.getName();
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public boolean isWatchForCreate() {
        return watchForCreate;
    }

    public void setWatchForCreate(final boolean watchForCreate) {
        this.watchForCreate = watchForCreate;
    }

    public boolean isWatchForModify() {
        return watchForModify;
    }

    public void setWatchForModify(final boolean watchForModify) {
        this.watchForModify = watchForModify;
    }

    public boolean isWatchForDelete() {
        return watchForDelete;
    }

    public void setWatchForDelete(final boolean watchForDelete) {
        this.watchForDelete = watchForDelete;
    }

    public boolean throwErrorOnOverflow() {
        return errorOnOverflow;
    }

    public void setErrorOnOverflow(final boolean errorOnOverflow) {
        this.errorOnOverflow = errorOnOverflow;
    }

    public boolean shouldWaitForWritingLocks() {
        return waitForWritingLocks;
    }

    public void setWaitForWritingLocks(final boolean waitForWritingLocks) {
        this.waitForWritingLocks = waitForWritingLocks;
    }

    public LocalFileEvent.FileFilter getFileFilter() {
        return fileFilter;
    }

    public void setFileFilter(final LocalFileEvent.FileFilter fileFilter) {
        this.fileFilter = fileFilter;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(final String index) {
        this.index = index;
    }
}