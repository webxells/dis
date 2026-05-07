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
package com.webxells.dis.officex.xlsx;

import com.webxells.dis.api.config.description.Description;
import java.util.stream.Stream;

public enum MultiValueStrategy {
    @Description("Uses first occurrence") FIRST {
        @Override
        public Stream<String> reduce(final Stream<String> stringStream) {
            return stringStream.findFirst().stream();
        }
    },

    @Description("Saves all columns as dataset pieces") MERGE {
        @Override
        public Stream<String> reduce(final Stream<String> stringStream) {
            return stringStream;
        }
    },

    @Description("Uses last occurrence") LAST {
        @Override
        public Stream<String> reduce(final Stream<String> stringStream) {
            return stringStream.reduce((a, b) -> b).stream();
        }
    };

    public abstract Stream<String> reduce(final Stream<String> stringStream);
}