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
package com.webxells.dis.json.intern;

import java.util.Objects;

public class Path {
    public interface Element {
    }

    public static abstract class NamedElement implements Element {
        public final String name;
        public NamedElement(final String name) {
            this.name = name;
        }
    }

    public interface Object extends Element { }

    public interface Array extends Element {
        public Integer getIndex();
    }

    public static class AnonymousObject implements Object {

    }

    public static class NamedObject extends NamedElement implements Object {
        public NamedObject(final String name) {
            super(name);
        }
    }

    public static class AnonymousArray implements Array {
        public final Integer index;
        public AnonymousArray(final Integer index) {
            this.index = index;
        }

        @Override
        public Integer getIndex() {
            return index;
        }
    }

    public static class NamedArray extends NamedElement implements Array {
        public final Integer index;
        public NamedArray(final String name, final Integer index) {
            super(name);
            this.index = index;
        }

        @Override
        public Integer getIndex() {
            return index;
        }
    }
}
