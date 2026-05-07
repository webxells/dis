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
package com.webxells.dis.config.json.parsing.plugins;

import com.webxells.dis.config.json.parsing.element.DisonMethodReader;
import com.webxells.dis.config.json.parsing.element.DisonStringReader;
import com.webxells.dis.test.cases.FileTestCase;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocalFileTest extends FileTestCase {

    @Test
    void test() throws IOException {
        File file = createFile();
        String value = random("some Test \"'");
        Files.writeString(file.toPath(), value);
        DisonMethodReader methodCall = new DisonMethodReader(null,
                List.of(new DisonStringReader(file.getAbsolutePath())), null);
        LocalFile fixture = new LocalFile();
        assertEquals(String.format("\"%s\"", value.replace("\"", "\\\"")),
                fixture.handle(methodCall, null).get().writeJson());
    }

}