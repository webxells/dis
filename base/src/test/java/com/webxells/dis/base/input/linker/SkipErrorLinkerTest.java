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
package com.webxells.dis.base.input.linker;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.input.linker.JoinLinker;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SkipErrorLinkerTest {
    static class TestException extends DisException {
        public TestException(final String message) {
            super(message);
        }
    }

    @Test
    void test() throws DisException {
        JoinLinker child = mock(JoinLinker.class);
        MappingConfiguration config = new SimpleMappingConfiguration();
        when(child.getData(same(config)))
                .thenThrow(InputOutputError.class)
                .thenThrow(InputOutputError.class);

        when(child.getInputName()).thenReturn("");

        doThrow(new TestException("")).when(child).start();
        doThrow(new TestException("")).when(child).end();

        SkipErrorLinker fixture = new SkipErrorLinker();
        assertThrows(InvalidApi.class, fixture::validate);
        fixture.setChild(child);

        assertDoesNotThrow(fixture::start);
        assertDoesNotThrow(() -> fixture.getData(config));
        assertDoesNotThrow(fixture::end);

        fixture.setEnableDataError(true);
        assertDoesNotThrow(fixture::start);
        assertThrows(InputOutputError.class, () -> fixture.getData(config));
        assertDoesNotThrow(fixture::end);

        fixture.setEnableBootError(true);
        assertThrows(TestException.class, fixture::start);
        assertThrows(InputOutputError.class, () -> fixture.getData(config));
        assertThrows(TestException.class, fixture::end);

    }

}