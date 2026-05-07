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

import java.util.Iterator;

public interface Marker {
    public final String ANY_MARKER = "*";

    public final String ANY_NON_NULL_MARKER = "+";

    public String getName();

    public void add(Marker reference);

    public boolean remove(Marker reference);

    public boolean hasChildren();

    public boolean hasReferences();

    public Iterator<Marker> iterator();

    public boolean contains(Marker other);

    public boolean contains(String name);

    public boolean equals(Object o);

    public int hashCode();

}