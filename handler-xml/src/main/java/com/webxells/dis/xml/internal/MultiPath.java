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
package com.webxells.dis.xml.internal;

import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.xml.internal.element.PathElement;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MultiPath extends XmlPath {
    private final Map<MappingPart, XmlPath> children;
    private final List<List<Map<XmlPath, XmlPath>>> values = new LinkedList<>();

    public MultiPath(final String path, final Map<MappingPart, XmlPath> children) throws XmlParsingError {
        super(path);
        this.children = children;
    }

    protected MultiPath(final LinkedList<PathElement> path,
                        final String pathString, final Map<MappingPart, XmlPath> children) {
        super(path, pathString);
        this.children = children;
    }

    @Override
    public XmlPath copy() {
        return new MultiPath(path, pathString, children);
    }

    public List<Map<XmlPath, XmlPath>> newTransaction() {
        final List<Map<XmlPath, XmlPath>> result = new LinkedList<>();
        values.add(result);
        return result;
    }

    public Map<MappingPart, XmlPath> getChildren() {
        return children;
    }

    public List<List<Map<XmlPath, XmlPath>>> getValues() {
        return values;
    }
}