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

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.test.cases.FileTestCase;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileExistsTest extends FileTestCase {

    @Test
    void test() throws IOException {
        DatasetPiece piece = new SimpleDatasetPiece("");
        File file1 = createFile();
        File file2 = createFile();
        File dir = new File(path(random()));
        dir.mkdir();

        FileExists fixture = new FileExists();

        fixture.setPath(file1.getPath());
        assertTrue(fixture.validate(piece, null));
        fixture.setPath(null);
        assertTrue(fixture.validate(new SimpleDatasetPiece(file2.getPath()), null));
        fixture.setPath(String.format("%s%s$current", file2.getParentFile(), File.separator));
        assertTrue(fixture.validate(new SimpleDatasetPiece(file2.getName()), null));

        fixture.setPath(dir.getPath());
        assertTrue(fixture.validate(piece, null));

        fixture.setAssertIsFile(true);
        assertFalse(fixture.validate(piece, null));
        fixture.setPath(file1.getPath());
        assertTrue(fixture.validate(piece, null));

        fixture.setAssertIsFile(false);
        fixture.setAssertIsDirectory(true);
        fixture.setPath(dir.getPath());
        assertTrue(fixture.validate(piece, null));
        fixture.setPath(file1.getPath());
        assertFalse(fixture.validate(piece, null));

    }

}