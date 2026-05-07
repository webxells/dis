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

import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.config.StableMappingPart;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RangeTest extends SimpleTestCase {

    @Test
    void testStep() throws InvalidDatasetException, InvalidApi {
        int from = randomMax(250) * 2;
        int to = from + randomMax(250) * 2;
        StableMappingPart part = new StableMappingPart(null);
        Range fixture = new Range();
        fixture.setFrom(from);
        fixture.setTo(to);
        fixture.setStep(2);
        fixture.setEdges(Range.Edges.FROM);
        fixture.validate();

        test(fixture, part, (to - from) / 2, from, 2);
    }

    @Test
    void testIncreasing() throws InvalidDatasetException {
        StableMappingPart part = new StableMappingPart(null);
        Range fixture = new Range();
        fixture.setFrom(5);
        fixture.setTo(10);

        fixture.setEdges(Range.Edges.NONE);
        test(fixture, part, 4, 6);

        fixture.setEdges(Range.Edges.FROM);
        test(fixture, part, 5, 5);

        fixture.setEdges(Range.Edges.TO);
        test(fixture, part, 5, 6);

        fixture.setEdges(Range.Edges.BOTH);
        test(fixture, part, 6, 5);
    }

    @Test
    void testDecreasing() throws InvalidDatasetException {
        StableMappingPart part = new StableMappingPart(null);
        Range fixture = new Range();
        fixture.setFrom(10);
        fixture.setTo(5);

        fixture.setEdges(Range.Edges.NONE);
        test(fixture, part, 4, 9, -1);

        fixture.setEdges(Range.Edges.FROM);
        test(fixture, part, 5, 10, -1);

        fixture.setEdges(Range.Edges.TO);
        test(fixture, part, 5, 9, -1);

        fixture.setEdges(Range.Edges.BOTH);
        test(fixture, part, 6, 10, -1);
    }

    private void test(final Range fixture, final StableMappingPart part, final int size, final int start) throws InvalidDatasetException {
        test(fixture, part, size, start, 1);
    }
    private void test(final Range fixture, final StableMappingPart part, final int size, final int start, final int step) throws InvalidDatasetException {
        fixture.manipulate(null, part);

        AtomicInteger expected = new AtomicInteger(start);
        assertEquals(size, part.getDataset().getContent().size());
        part.getDataset().getContent()
                .forEach(a -> assertEquals(expected.getAndAdd(step), Integer.valueOf(a.value().get())));
    }


}