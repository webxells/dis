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
package com.webxells.dis.hash;

import com.webxells.dis.api.hash.Engine;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.function.BiFunction;

public enum HashStrategy {
    SIMPLE {
        @Override
        String run(final Engine engine, final String data) {
            return engine.convert(data);
        }

        @Override
        String run(final Engine engine, final InputStream data) throws IOException {
            return engine.convert(data);
        }
    },
    DOUBLE {
        @Override
        String run(final Engine engine, final String data) {
            return engine.convert(data).concat(engine.convert(sortData(data)));
        }

        @Override
        String run(final Engine engine, final InputStream data) {
            throw new UnsupportedOperationException("Makes no sense");
        }
    };

    private static String sortData(final String data) {
        final StringBuilder result = new StringBuilder(data.length());
        data.codePoints()
            .sorted()
            .forEach(result::append);
        return result.toString();
    }

    abstract String run(final Engine engine, final String data);

    abstract String run(final Engine engine, final InputStream data) throws IOException;
}