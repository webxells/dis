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
package com.webxells.dis.base.manipulator;

import com.webxells.dis.api.config.description.Description;

@Description("Provides various math operations for natural numbers")
public class MathLong extends Math<Long> {
    public static class LongWrapper implements NumberWrapper<Long> {
        private final long value;

        private LongWrapper(final long value) {
            this.value = value;
        }

        @Override
        public NumberWrapper<Long> add(final NumberWrapper<Long> b) {
            return new LongWrapper(value + b.value());
        }

        @Override
        public NumberWrapper<Long> sub(final NumberWrapper<Long> b) {
            return new LongWrapper(value - b.value());
        }

        @Override
        public Long value() {
            return value;
        }

        @Override
        public String format() {
            return String.valueOf(value);
        }
    }


    @Override
    protected NumberWrapper<Long> boxed(final String value) {
        if (value.codePoints().allMatch(Character::isDigit)) {
            return new LongWrapper(Long.parseLong(value));
        }
        return null;
    }
}