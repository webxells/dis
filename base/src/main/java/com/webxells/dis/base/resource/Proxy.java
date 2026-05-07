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
package com.webxells.dis.base.resource;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.api.validator.Validator;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class Proxy implements Resource, Validator {
    public enum OverwriteType {
        APPEND, REPLACE, ERROR
    }
    public enum NotFoundStrategy {
        EMPTY, ERROR
    }

    private static final Map<String, Queue<String>> REGISTRY = new HashMap<>();

    private String index;
    private NotFoundStrategy notFoundStrategy = NotFoundStrategy.ERROR;
    private OverwriteType overwriteType = OverwriteType.APPEND;

    @Override
    public OutputStream send() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InputStream receive() {
        return new ByteArrayInputStream(getNext().getBytes());
    }

    private String getNext() {
        return Optional.ofNullable(REGISTRY.get(index))
                .map(Queue::poll)
                .orElseGet(this::notFound);
    }

    private String notFound() {
        return switch (notFoundStrategy) {
            case EMPTY -> "";
            case ERROR -> throw new RuntimeException("Could not find content");
        };
    }

    @Override
    public boolean validate(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        final Queue<String> queue = REGISTRY.computeIfAbsent(index, a -> new LinkedBlockingQueue<>());
        final String content = currentPiece.value()
                .orElseGet(this::notFound);
        if (0 < queue.size()) {
            switch (overwriteType) {
                case REPLACE -> queue.clear();
                case ERROR -> throw new RuntimeException("Could not overwrite content");
            }
        }
        queue.add(content);
        return true;
    }

    public void setIndex(final String index) {
        this.index = index;
    }

    public void setNotFoundStrategy(final NotFoundStrategy notFoundStrategy) {
        this.notFoundStrategy = notFoundStrategy;
    }

    public void setOverwriteType(final OverwriteType overwriteType) {
        this.overwriteType = overwriteType;
    }
}