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
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SingleToMultiManipulationTest extends SimpleTestCase {

    @Test
    void test() throws InvalidDatasetException {
        String value = random();
        Overwrite overwrite = new Overwrite();
        overwrite.setAppend(true);
        overwrite.setValue(value);
        DatasetPiece piece = new SimpleDatasetPiece(null);
        MappingPart part = new SimpleMappingPart(null);
        SingleToMultiManipulation fixture = new SingleToMultiManipulation();
        assertThrows(InvalidApi.class, fixture::validate);
        fixture.setChildren(List.of(overwrite, overwrite, overwrite, new ConcatenationSingleCall()));

        fixture.manipulate(piece, part);

        assertEquals(value.repeat(3), part.value().get());
    }

}