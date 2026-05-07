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
package com.webxells.dis.localfile.validator;

import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.test.cases.FileTestCase;
import java.io.File;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MakeDirTest extends FileTestCase {
    @Test
    void test() {
        String dirName = random();
        String dirPath = path(dirName);
        MakeDir fixture = new MakeDir();
        fixture.setPath(dirPath);
        fixture.setCreatePath(false);

        File dir = new File(dirPath);
        assertFalse(dir.exists());
        assertTrue(fixture.validate(new SimpleDatasetPiece(random()), null));
        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());

        dir.delete();
        assertFalse(dir.exists());
        fixture.setPath(path().concat(File.separator).concat("$current"));
        assertTrue(fixture.validate(new SimpleDatasetPiece(dirName), null));
        assertTrue(dir.exists());

        assertTrue(fixture.validate(new SimpleDatasetPiece(dirName), null));
    }

}