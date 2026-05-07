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
package com.webxells.dis.time.manipulator;

import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GetDateTimeTest extends SimpleTestCase {

    @Test
    void test() throws InvalidDatasetException, InterruptedException {
        MappingPart part = newConfiguration()
                .addPart(new ConfigurationBuilder.PartBuilder()
                        .withContent())
                .build().parts().get(0);
        GetDateTime fixture = new GetDateTime();
        fixture.setFormat("yyyy 'yyyy' '' HH AA");
        fixture.manipulate(null, part);

        assertEquals(1, part.getDataset().getContent().size());
        String firstResult = part.value().get();
        assertTrue(firstResult.matches("\\d{4} yyyy ' \\d{2} \\d+"));

        fixture.manipulate(null, part);
        assertEquals(firstResult, part.value().get());


        if (LocalDateTime.now(ZoneId.of("GMT")).format(DateTimeFormatter.ofPattern("mm")).equals("59")) {
            Thread.sleep(60 * 1000);
        }


        boolean minus = Integer.parseInt(
                LocalDateTime.now(ZoneId.of("GMT")).format(DateTimeFormatter.ofPattern("HH"))) > 21;

        fixture.setLiveUpdate(true);
        fixture.setFormat("HH");
        fixture.setZone("GMT");
        fixture.manipulate(null, part);
        int gmtTime = Integer.parseInt(part.value().get());
        fixture.setZone(minus ? "GMT-2" : "GMT+2");
        fixture.manipulate(null, part);
        int otherZoneTime = Integer.parseInt(part.value().get());
        assertEquals(gmtTime + (2 * (minus ? -1 : 1)), otherZoneTime);

    }

}