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
package com.webxells.dis.xml;

import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.resource.Resource;
import java.util.List;

@Description("Xml reader")
public class XmlConfig implements InputConfig {
    public enum UnexpectedMultipleOccurrenceStrategy {
        @Description("Raises error") FAIL, SELECT_FIRST, SELECT_LAST;
    }

    public enum UnreachablePathStrategy {
        @Description("Raises error") FAIL, @Description("Null") NULL, @Description("Empty String")EMPTY;
    }

    @Description("Reference for this handler")
    @Required
    private String name;
    @Description("Defines where to receive the xml data from")
    @Required
    private Resource receiver;
    @Description("Path to iterate over and match mapping paths")
    @Required
    private String iterationPath;
    @Description("Decides which value to pick when multiple values were found, but just one expected")
    @Default("SELECT_FIRST")
    private UnexpectedMultipleOccurrenceStrategy unexpectedMultipleOccurrenceStrategy =
            UnexpectedMultipleOccurrenceStrategy.SELECT_FIRST;
    @Description("Value for invalid or nonexistent paths")
    @Default("Null")
    private UnreachablePathStrategy unreachablePathStrategy = UnreachablePathStrategy.NULL;
    @Description("Xml paths that are read as raw xml")
    private List<String> rawReads;
    @Description("Ignores dtd (document type definition)")
    @Default("false")
    private boolean ignoreDtd;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return XmlInput.class.getName();
    }

    public List<String> getRawReads() {
        return rawReads;
    }

    public void setRawReads(final List<String> rawReads) {
        this.rawReads = rawReads;
    }
    public void setName(final String name) {
        this.name = name;
    }

    public Resource getReceiver() {
        return receiver;
    }

    public void setReceiver(final Resource receiver) {
        this.receiver = receiver;
    }

    public String getIterationPath() {
        return iterationPath;
    }

    public void setIterationPath(final String iterationPath) {
        this.iterationPath = iterationPath;
    }

    public UnexpectedMultipleOccurrenceStrategy getUnexpectedMultipleOccurrenceStrategy() {
        return unexpectedMultipleOccurrenceStrategy;
    }

    public void setUnexpectedMultipleOccurrenceStrategy(final UnexpectedMultipleOccurrenceStrategy unexpectedMultipleOccurrenceStrategy) {
        this.unexpectedMultipleOccurrenceStrategy = unexpectedMultipleOccurrenceStrategy;
    }

    public UnreachablePathStrategy getUnreachablePathStrategy() {
        return unreachablePathStrategy;
    }

    public void setUnreachablePathStrategy(final UnreachablePathStrategy unreachablePathStrategy) {
        this.unreachablePathStrategy = unreachablePathStrategy;
    }

    public boolean isIgnoreDtd() {
        return ignoreDtd;
    }

    public void setIgnoreDtd(final boolean ignoreDtd) {
        this.ignoreDtd = ignoreDtd;
    }
}