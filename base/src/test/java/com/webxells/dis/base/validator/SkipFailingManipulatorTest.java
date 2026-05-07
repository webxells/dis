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
package com.webxells.dis.base.validator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.Manipulator;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.manipulator.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class SkipFailingManipulatorTest extends ValidatorsTest {

    @Test
    void testTrue() {
        SkipFailingManipulator fixture = new SkipFailingManipulator();
        fixture.setManipulator(mock(Manipulator.class));
        assertTrue(fixture.validateCurrentPiece(createDatasetPiece(null), new SimpleMappingPart(null)));
    }

    @Test
    void testFalse() throws InvalidDatasetException {
        DatasetPiece datasetPiece = createDatasetPiece("Test");
        SimpleMappingPart simpleMappingPart = new SimpleMappingPart(null);


        Manipulator manipulator = mock(Manipulator.class);
        doThrow(NullPointerException.class)
                .when(manipulator)
                .manipulate(datasetPiece, simpleMappingPart);

        SkipFailingManipulator fixture = new SkipFailingManipulator();
        fixture.setManipulator(manipulator);
        assertFalse(fixture.validateCurrentPiece(datasetPiece, simpleMappingPart));
    }

}
