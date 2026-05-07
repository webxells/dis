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
package com.webxells.dis.rest.manipulator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.test.cases.SimpleTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Base64Test extends SimpleTestCase {

    @Test
    void test() throws InvalidDatasetException {
        Base64 fixture = new Base64();
        String value = "asdasd123123csdfsdfsdfsdfs";
        DatasetPiece piece = new SimpleDatasetPiece(value);
        fixture.manipulate(piece, null);
        assertEquals("YXNkYXNkMTIzMTIzY3NkZnNkZnNkZnNkZnM=", piece.value().get());

        fixture.setStrategy(Base64.Strategy.DECODE);
        fixture.manipulate(piece, null);
        assertEquals(value, piece.value().get());

        fixture.setWithoutPadding(true);
        fixture.setStrategy(Base64.Strategy.ENCODE);
        fixture.manipulate(piece, null);
        assertEquals("YXNkYXNkMTIzMTIzY3NkZnNkZnNkZnNkZnM", piece.value().get());

        fixture.setStrategy(Base64.Strategy.DECODE);
        fixture.manipulate(piece, null);
        assertEquals(value, piece.value().get());
    }

}