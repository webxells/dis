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
package com.webxells.dis.test.example.discover;

import com.webxells.dis.api.discover.DiscoverSummary;
import com.webxells.dis.api.discover.ExplorerSummary;
import com.webxells.dis.api.discover.PathDiscoverer;
import java.util.List;

public class SimpleDiscoverySummary implements DiscoverSummary {
    private long size;
    private String murmur3;
    private ExplorerSummary explorerSummary;
    private List<PathDiscoverer.Run> pathDiscoverer;

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public String getMurMur3() {
        return murmur3;
    }

    @Override
    public ExplorerSummary getExplorerSummary() {
        return explorerSummary;
    }

    @Override
    public List<PathDiscoverer.Run> getPathDiscoverer() {
        return pathDiscoverer;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    @Override
    public void setMurMur3(final String murMur3) { murmur3 = murMur3; }


    public void setExplorerSummary(final ExplorerSummary explorerSummary) {
        this.explorerSummary = explorerSummary;
    }

    public void setPathDiscoverer(final List<PathDiscoverer.Run> pathDiscoverer) {
        this.pathDiscoverer = pathDiscoverer;
    }
}