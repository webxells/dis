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
package org.slf4j;

import java.util.Collections;
import java.util.Iterator;

public class NullMarker implements Marker {
    @Override
    public String getName() {
        return "null";
    }

    @Override
    public void add(final Marker reference) { }

    @Override
    public boolean remove(final Marker reference) {
        return true;
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public boolean hasReferences() {
        return false;
    }

    @Override
    public Iterator<Marker> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public boolean contains(final Marker other) {
        return false;
    }

    @Override
    public boolean contains(final String name) {
        return false;
    }
}