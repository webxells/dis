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
package com.webxells.dis.plain.manipulator;

import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.test.cases.SimpleTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeleteRangeTest extends SimpleTestCase {

    @Test
    void test() throws InvalidApi, InvalidDatasetException {
        test("2", "4", "012456789", false);
        test("2", "4", "0156789", true);
        test(null, "4", "456789", false);
        test(null, "4", "56789", true);
        test("2", null, "012", false);
        test("2", null, "01", true);
        test("2", null, "01", true);

        test("a", null, "0123456789", DeleteRange.NotFoundStrategy.IGNORE);
        test(null, "a", "0123456789", DeleteRange.NotFoundStrategy.IGNORE);
        test("a", null, "", DeleteRange.NotFoundStrategy.BLANC);
        test(null, "a", "", DeleteRange.NotFoundStrategy.BLANC);
        assertThrows(InvalidDatasetException.class, () -> test("a", null, "", DeleteRange.NotFoundStrategy.ERROR));
        assertThrows(InvalidDatasetException.class, () -> test(null, "a", "", DeleteRange.NotFoundStrategy.ERROR));
    }

    private void test(final String from, final String to, final String expected, final DeleteRange.NotFoundStrategy notFoundStrategy) throws InvalidApi, InvalidDatasetException {
        DeleteRange fixture = buildFixture(from, to, true);
        fixture.setNotFoundStrategy(notFoundStrategy);
        assertExpected(expected, fixture);
    }

    private void assertExpected(final String expected, final DeleteRange fixture) throws InvalidDatasetException {
        SimpleDatasetPiece datasetPiece = new SimpleDatasetPiece("0123456789");
        fixture.manipulate(datasetPiece, null);

        assertEquals(expected, datasetPiece.value().get());
    }

    private void test(final String from,  final String to, final String expected, final boolean deleteEdge) throws InvalidDatasetException, InvalidApi {
        DeleteRange fixture = buildFixture(from, to, deleteEdge);
        assertExpected(expected, fixture);
    }

    private DeleteRange buildFixture(final String from, final String to, final boolean deleteEdge) throws InvalidApi {
        DeleteRange fixture = new DeleteRange();
        assertThrows(InvalidApi.class, fixture::validate);
        fixture.setFrom(from);
        fixture.setTo(to);
        fixture.validate();
        fixture.setDeleteEdges(deleteEdge);
        return fixture;
    }

}