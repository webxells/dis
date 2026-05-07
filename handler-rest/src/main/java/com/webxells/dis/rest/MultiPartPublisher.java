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
package com.webxells.dis.rest;

import com.webxells.dis.api.BinaryData;
import com.webxells.dis.rest.execution.BodyPublisher;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class MultiPartPublisher implements BodyPublisher {
    private final MultiPartInputStream multiPartInputStream;
    private final int boundaryLength;

    public MultiPartPublisher(final MultiPartInputStream multiPartInputStream) {
        this.multiPartInputStream = multiPartInputStream;
        boundaryLength = multiPartInputStream.createSeparator().length();
    }

    @Override
    public long contentLength() {
        final Set<Map.Entry<String, BinaryData>> values = multiPartInputStream.getParts().entrySet();
        assertAllSizesAreKnown(values);
        return values.stream()
                .mapToLong(this::calculateSize)
                /* terminating boundary
                --
                 */
                .sum() + boundaryLength + 2;
    }

    @Override
    public InputStream get() {
        return multiPartInputStream;
    }

    private long calculateSize(final Map.Entry<String, BinaryData> entry) {
        final BinaryData binaryData = entry.getValue();
        final AtomicLong result = new AtomicLong(binaryData.getSize());
        /*
        ;filename="%s"
         */
        Optional.ofNullable(binaryData.getName())
                .ifPresent(a -> result.addAndGet(a.length() + 12));
        /*
        %nContent-Type:%s%n%n
         */
        result.addAndGet(binaryData.getMimeType().length() + 19);
        /*
        Content-Disposition:form-data;name="%s"
         */
        result.addAndGet(entry.getKey().length() + 37);
        //boundary + 2 * LineEnding
        return result.addAndGet(boundaryLength + 4);
    }

    private void assertAllSizesAreKnown(final Set<Map.Entry<String, BinaryData>> values) {
        if (values.stream().anyMatch(a -> a.getValue().getSize() < 0)) {
            throw new RuntimeException(
                    "No size found for BinaryData - use chunked transfer encoding instead");
        }
    }
}