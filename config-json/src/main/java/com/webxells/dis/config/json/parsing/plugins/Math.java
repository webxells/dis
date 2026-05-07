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
package com.webxells.dis.config.json.parsing.plugins;

import com.webxells.dis.api.Logger;
import com.webxells.dis.config.json.parsing.DisonElementReader;
import com.webxells.dis.config.json.parsing.DisonJsonTransformer;
import com.webxells.dis.config.json.parsing.element.DisonDoubleReader;
import com.webxells.dis.config.json.parsing.element.DisonLongReader;
import com.webxells.dis.config.json.parsing.element.DisonMethodReader;
import com.webxells.dis.config.json.parsing.plugins.math.*;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.util.List;
import java.util.Optional;

public class Math implements DisonPlugin {
    public static class Manager implements DisonPluginManager {
        @Override
        public DisonPlugin isCompetent(final String disCommand) {
            return switch (disCommand) {
                case "--dis-add" -> new Math(Add.instance());
                case "--dis-sub" -> new Math(Sub.instance());
                case "--dis-multiply" -> new Math(Multiply.instance());
                case "--dis-round" -> new Math(Round.instance());
                default -> null;
            };
        }
    }

    private static final Logger LOGGER = LoggerProxyFactory.logger(Math.class);

    private final Operation operation;

    private Math(final Operation operation) {
        this.operation = operation;
    }

    @Override
    public Optional<DisonElementReader> handle(final DisonElementReader disonElementReader, final DisonJsonTransformer disonJsonTransformer) {
        if (disonElementReader instanceof DisonMethodReader methodReader) {
            final List<DisonElementReader> parameters = methodReader.getParameters();
            if (operation instanceof BiParameterOperation biOperation) {
                return Optional.of(biOperation.operate(getParameter(parameters, 0, disonJsonTransformer),
                        getParameter(parameters, 1, disonJsonTransformer)));
            }
            if (operation instanceof SingleParameterOperation singleOperation) {
                return Optional.of(singleOperation.operate(getParameter(parameters, 0, disonJsonTransformer)));
            }
        }
        return Optional.empty();
    }

    private MathOperand<?> getParameter(final List<DisonElementReader> parameters, final int index,
                                        final DisonJsonTransformer disonJsonTransformer) {
        if (index < parameters.size()) {
            DisonElementReader disonElementReader = parameters.get(index);
            while (disonElementReader instanceof DisonMethodReader methodReader) {
                disonElementReader = methodReader.call();
            }
            if (disonElementReader instanceof DisonLongReader longType) {
                return new LongOperand(longType.read());
            }
            if (disonElementReader instanceof DisonDoubleReader doubleType) {
                return new DoubleOperand(doubleType.read());
            }
            disonJsonTransformer.plugInError(String.format("Invalid parameter type for index %d: %s", index, disonElementReader.describeSelf()));
        } else {
            disonJsonTransformer.plugInError(String.format("Not enough parameters: %d", index));
        }
        LOGGER.w("Just taking 0");
        return new LongOperand(0);
    }

}