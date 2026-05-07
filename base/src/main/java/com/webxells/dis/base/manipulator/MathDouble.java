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
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

@Description("Provides various math operations for decimal numbers")
public class MathDouble extends Math<BigDecimal> {
    public static class DoubleWrapper implements NumberWrapper<BigDecimal> {
        private final BigDecimal value;

        private DoubleWrapper(final BigDecimal value) {
            this.value = value;
        }

        @Override
        public NumberWrapper<BigDecimal> add(final NumberWrapper<BigDecimal> b) {
            return new DoubleWrapper(value.add(b.value()));
        }

        @Override
        public NumberWrapper<BigDecimal> sub(final NumberWrapper<BigDecimal> b) {
            return new DoubleWrapper(value.subtract(b.value()));
        }

        @Override
        public BigDecimal value() {
            return value;
        }

        @Override
        public String format() {
            return value.toPlainString();
        }
    }

    public static final NumberFormat FORMAT = DecimalFormat.getInstance(Locale.US);


    @Override
    protected NumberWrapper<BigDecimal> boxed(final String value) {
        try {
            return new DoubleWrapper(new BigDecimal(value));
        } catch (final NumberFormatException  ignored) {
            //add check in future releases
            return null;
        }
    }
}