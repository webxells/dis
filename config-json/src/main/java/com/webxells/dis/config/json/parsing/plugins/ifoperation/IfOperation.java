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
package com.webxells.dis.config.json.parsing.plugins.ifoperation;

import com.webxells.dis.config.json.parsing.DisonElementReader;
import com.webxells.dis.config.json.parsing.DisonJsonTransformer;
import com.webxells.dis.config.json.parsing.element.DisonBooleanReader;
import com.webxells.dis.config.json.parsing.element.DisonMethodReader;
import com.webxells.dis.config.json.parsing.element.DisonNullReader;
import com.webxells.dis.config.json.parsing.element.DisonPrimitiveReader;
import com.webxells.dis.config.json.parsing.plugins.DisonPlugin;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public abstract class IfOperation implements DisonPlugin {

    @Override
    public Optional<DisonElementReader> handle(final DisonElementReader disonElementReader,
                                               final DisonJsonTransformer disonJsonTransformer) {
        final AtomicReference<Object> reference = new AtomicReference<>();
        return Optional.of(new DisonBooleanReader(
                getAsMethodReader(disonElementReader, disonJsonTransformer).stream()
                        .flatMap(a -> a.getParameters().stream())
                        .reduce(true,
                                (result, element) -> result && compareAndSet(element, reference, disonJsonTransformer),
                                (current, next) -> current && next) && null != reference.get()));
    }

    protected abstract boolean ifOperate(final Object a, final AtomicReference<Object> reference);

    protected Optional<DisonMethodReader> getAsMethodReader(final DisonElementReader disonElementReader, final DisonJsonTransformer disonJsonTransformer) {
        if (disonElementReader instanceof DisonMethodReader methodReader) {
            return Optional.of(methodReader);
        }
        disonJsonTransformer.plugInError("Only as method");
        return Optional.empty();
    }


    protected boolean compareAndSet(final DisonElementReader element, final AtomicReference<Object> reference,
                                  final DisonJsonTransformer disonJsonTransformer) {
        return getValue(element, disonJsonTransformer)
                .map(a -> {
                    if (null == reference.get()) {
                        reference.set(a);
                    }
                    return ifOperate(a, reference);
                })
                .orElse(false);
    }

    private Optional<Object> getValue(DisonElementReader elementReader, final DisonJsonTransformer disonJsonTransformer) {
        while (elementReader instanceof DisonMethodReader method) {
            elementReader = method.call();
        }
        if (elementReader instanceof DisonPrimitiveReader<?> primitiveReader) {
            return Optional.of(primitiveReader.read());
        }
        if (elementReader instanceof DisonNullReader) {
            return Optional.of(elementReader);
        }
        disonJsonTransformer.plugInError("Only primitives are allowed to compare, got: ".concat(elementReader.getType().name()));
        return Optional.empty();
    }

}