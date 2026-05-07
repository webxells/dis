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

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExtendValueTest extends SimpleTestCase {

    @Test
    void test() {
        testFixture(null, "value", "extension", "valueextension");
        testFixture(null, null, "extension", "extension");
        testFixture("asd", "value", "extension", "asd");
        testFixture("$asd", "value", "extension", "$asd");
        testFixture("$new - $old - $new", "value", "extension", "extension - value - extension");
        testFixture("$old$new", "$old$new", "$old$new", "$old$new$old$new");
        testFixture("$new$old", "$old$new", "$old$new", "$old$new$old$new");
    }

    private void testFixture(final String extendRule, final String old, final String extension, final String newValue) {
        String name = random();
        String path = random();
        MappingConfiguration config = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(name, path)
                        .withContent(extension)
                )
                .build();
        SimpleDatasetPiece piece = new SimpleDatasetPiece(old);
        ExtendValue fixture = new ExtendValue();
        fixture.setValue(extension);
        if (null != extendRule) {
            fixture.setExtendRule(extendRule);
        }
        fixture.manipulate(piece, config.parts().get(0));
        assertEquals(newValue, piece.value().get());

        piece.rewriteValue(old);
        fixture.setValue(random());
        fixture.setValuePortrayal(new SimpleMappingPortrayal(name, path));
        fixture.manipulate(piece, config.parts().get(0));
        assertEquals(newValue, piece.value().get());
    }

}