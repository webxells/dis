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
package com.webxells.dis.config.json.parsing.plugins.math;

import com.webxells.dis.config.json.parsing.DisonElementReader;
import com.webxells.dis.config.json.parsing.element.DisonDoubleReader;
import com.webxells.dis.config.json.parsing.element.DisonLongReader;

public abstract class BiParameterOperation extends ParameterOperation {
    public abstract double operateDouble(final double left, final double right);

    public DisonElementReader operate(final MathOperand<?> left, final MathOperand<?> right) {
        final Double result = operateDouble(getAsDouble(left).get(), getAsDouble(right).get());
        final long asLong = result.longValue();
        if (result - asLong != 0) {
            return new DisonDoubleReader(result);
        }
        return new DisonLongReader(asLong);
    }

    @Override
    public int parameterCount() {
        return 2;
    }
}