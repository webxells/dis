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
package com.webxells.dis.fileregistry;

import com.webxells.dis.fileregistry.internal.Murmur3;
import java.util.function.BiFunction;

public enum MurMur3Strategy {
    SIMPLE(Murmur3::hash128),
    DOUBLE(Murmur3::doubleHashed128);

    private final BiFunction<String, Integer, String> hashStrategy;

    MurMur3Strategy(final BiFunction<String, Integer, String> hashStrategy) {
        this.hashStrategy = hashStrategy;
    }

    public String createHash(final String string, final int seed) {
        return hashStrategy.apply(string, seed);
    }
}
