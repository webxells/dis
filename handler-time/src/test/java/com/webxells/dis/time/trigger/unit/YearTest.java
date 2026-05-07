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
package com.webxells.dis.time.trigger.unit;

import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class YearTest extends SimpleTestCase {

    @Test
    void test() throws InvalidApi {
        Year fixture = new Year();
        fixture.setValue(1000);
        assertThrows(InvalidApi.class, fixture::validate);
        fixture.setValue(-23);
        assertThrows(InvalidApi.class, fixture::validate);
        fixture.setValue(0);
        assertThrows(InvalidApi.class, fixture::validate);
        int futureYear = LocalDateTime.now().getYear() + randomMax(200) + 1;
        fixture.setValue(futureYear);
        fixture.validate();
        assertThrows(CalculationException.class, () -> fixture.increase(null));
        LocalDateTime localDateTime = mock(LocalDateTime.class);
        fixture.calcNextRunForCurrent(localDateTime);
        verify(localDateTime).withYear(eq(futureYear));
    }

}