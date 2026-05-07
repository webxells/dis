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
package com.webxells.dis.api.discover;

import java.util.List;

public interface DiscoverSummary {
    long getSize();

    String getMurMur3();

    ExplorerSummary getExplorerSummary();

    List<PathDiscoverer.Run> getPathDiscoverer();

    void setSize(long size);

    void setMurMur3(String murMur3);

    void setExplorerSummary(ExplorerSummary explorerSummary);

    void setPathDiscoverer(List<PathDiscoverer.Run> pathDiscoverer);
}