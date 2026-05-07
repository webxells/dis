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

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.manipulator.Manipulator;

@Description("Provides various math operations")
public abstract class Math<R> implements Manipulator {
    public interface NumberWrapper<T> {
        public NumberWrapper<T> add(final NumberWrapper<T> b);
        public NumberWrapper<T> sub(final NumberWrapper<T> b);
        public T value();
        public String format();
    }

    public enum Operation {
        ADD {
            @Override <T> NumberWrapper<T> applyFunction(NumberWrapper<T> a, NumberWrapper<T> b) {
                return a.add(b);
            }
        },
        SUBTRACT {
            @Override <T> NumberWrapper<T> applyFunction(NumberWrapper<T> a, NumberWrapper<T> b) {
                return a.sub(b);
            }
        },
        SUBTRACT_REVERSE {
            @Override <T> NumberWrapper<T> applyFunction(NumberWrapper<T> a, NumberWrapper<T> b) {
                return b.sub(a);
            }
        };

        abstract <T> NumberWrapper<T> applyFunction(NumberWrapper<T> a, NumberWrapper<T> b);

        <T> NumberWrapper<T> apply(final NumberWrapper<T> first, final NumberWrapper<T> second) {
            return applyFunction(first, second);
        }
    }

    @Required
    private Operation operation;
    @Required(xor = {"operandPortrayal"})
    @Description("Value to operate with")
    private String operand;
    @Required(xor = {"operand"})
    @Description("MappingPart that holds the operand")
    private MappingPortrayal operandPortrayal;

    @Override
    public void validate() throws InvalidApi {
        if ((null == operand && null == operandPortrayal) || null == operation) {
            throw new InvalidApi("required fields missing");
        }
    }

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        currentPiece.value()
                .map(this::boxed)
                .ifPresent(a -> currentPiece.rewriteValue(
                        operation.apply(a, getOperand(mappingPart)).format()));
    }


    protected abstract NumberWrapper<R> boxed(final String value);

    private NumberWrapper<R> getOperand(final MappingPart mappingPart) {
        if (null != operand) {
            return boxed(operand);
        }
        return mappingPart.getConfiguration().getByPortrayal(operandPortrayal)
                .flatMap(MappingPart::value)
                .map(this::boxed)
                .orElseThrow(() -> new RuntimeException("Could not find any value"));
    }

    public void setOperation(final Operation operation) {
        this.operation = operation;
    }

    public void setOperand(final String operand) {
        this.operand = operand;
    }

    public void setOperandPortrayal(final MappingPortrayal operandPortrayal) {
        this.operandPortrayal = operandPortrayal;
    }
}