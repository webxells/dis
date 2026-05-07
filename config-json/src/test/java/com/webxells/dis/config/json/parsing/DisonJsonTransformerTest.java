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
package com.webxells.dis.config.json.parsing;

import com.webxells.dis.config.json.intern.SourceMapping;
import com.webxells.dis.config.json.parsing.plugins.ForeachParser;
import com.webxells.dis.config.json.parsing.plugins.If;
import com.webxells.dis.config.json.parsing.plugins.IncludeParser;
import com.webxells.dis.config.json.parsing.plugins.Random;
import com.webxells.dis.config.json.parsing.plugins.TemplateParser;
import com.webxells.dis.config.json.parsing.plugins.VarParser;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DisonJsonTransformerTest extends SimpleTestCase {
    private static final ClassLoader LOADER = DisonJsonTransformerTest.class.getClassLoader();
    private static Path workingDir;

    @BeforeAll
    static void setUp() {
        System.setProperty("dison-source-mapping-key", "test");
        workingDir = new File(Objects.requireNonNull(LOADER.getResource("parsing/include-as-children-main.json")).getPath())
                .toPath()
                .toAbsolutePath()
                .getParent();
    }

    @Test
    void testBugTemplateVars() throws IOException {
        DisonJsonTransformer fixture = new DisonBuilder()
                .addAllFeatures()
                .workingDirectory(workingDir)
                .build();

        passEqualsTest(fixture, "bug-template-vars");
    }

    @Test
    void testBugIncludedIf() throws IOException {
        DisonJsonTransformer fixture = new DisonBuilder()
                .addFeature(new If.Manager())
                .addFeature(new IncludeParser.Manager())
                .addFeature(new VarParser.Manager())
                .mapConfiguration()
                .workingDirectory(workingDir)
                .build();

        //ignore all paths
        passEqualsTest(fixture, "bug-included-if", a -> a.replaceAll(String.format(
                "(\"%s\":\")[^\"]+%s([^\"]+\\.[^\"]+\")", SourceMapping.getMappingKey(), File.separator),"$1$2"));
    }

    @Test
    void testBugNestedIf() throws IOException {
        DisonJsonTransformer fixture = new DisonBuilder()
                .addFeature(new If.Manager())
                .addFeature(new VarParser.Manager())
                .workingDirectory(workingDir)
                .build();

        passEqualsTest(fixture, "bug-nested-if");
    }

    @Test
    void testBugDoubleEscape() throws IOException {
        DisonJsonTransformer fixture = new DisonBuilder()
                .addFeature(new IncludeParser.Manager())
                .addFeature(new VarParser.Manager())
                .workingDirectory(workingDir)
                .build();

        passEqualsTest(fixture, "bug-double-escape");
    }

    @Test
    void testConfigurationMapping() throws IOException {
        DisonJsonTransformer fixture = new DisonBuilder()
                .mapConfiguration()
                .workingDirectory(workingDir)
                .build();

        passEqualsTest(fixture, "mapConfig");
    }

    @Test
    void testForeach() throws IOException {
        DisonJsonTransformer fixture = new DisonBuilder()
                .addFeature(new ForeachParser.Manager())
                .workingDirectory(workingDir)
                .build();

        passEqualsTest(fixture, "foreach");
    }

    @Test
    void testForeachTemplate() throws IOException {
        DisonJsonTransformer fixture = new DisonBuilder()
                .addFeature(new VarParser.Manager())
                .addFeature(new TemplateParser.Manager())
                .addFeature(new ForeachParser.Manager())
                .addFeature(new If.Manager())
                .workingDirectory(workingDir)
                .build();

        passEqualsTest(fixture, "foreach-template");
    }

    @Test
    void testTemplate() throws IOException {
        DisonJsonTransformer fixture = new DisonBuilder()
                .addFeature(new VarParser.Manager())
                .addFeature(new TemplateParser.Manager())
                .workingDirectory(workingDir)
                .build();

        passEqualsTest(fixture, "template-main");
    }

    @Test
    void testVarDefaultSavingIntoTemp() throws IOException {
        DisonJsonTransformer fixture = new DisonBuilder()
                .addFeature(new VarParser.Manager())
                .addFeature(new IncludeParser.Manager())
                .workingDirectory(workingDir)
                .build();

        passEqualsTest(fixture, "child-default-var");
    }

    @Test
    void testRandomParser() {
        DisonJsonTransformer fixture = new DisonBuilder()
                .addFeature(new Random.Manager())
                .addFeature(new VarParser.Manager())
                .build();
        for (int i = randomMax(10000) + 1; i-->0;) {
            String result = fixture.parse(
                    "{'--dis-var': {\"var\":'--dis-random()'}, \"direct\":'--dis-random(1000)', \"var\":'--dis-var(\"var\")'}");
            assertTrue(result.matches("\\{ \"direct\":\\d{1,4}, \"var\":\\d+}"));
        }
    }

    @Test
    void testVarDelete() throws IOException {
        DisonJsonTransformer fixture = new DisonBuilder()
                .addFeature(new If.Manager())
                .addFeature(new VarParser.Manager())
                .workingDirectory(workingDir)
                .build();

        passEqualsTest(fixture, "simple-variable-delete");
    }

    @Test
    void testIf() throws IOException {
        DisonJsonTransformer fixture = new DisonBuilder()
                .addFeature(new If.Manager())
                .addFeature(new VarParser.Manager())
                .addFeature(new IncludeParser.Manager())
                .workingDirectory(workingDir)
                .build();

        passEqualsTest(fixture, "if");
    }

    @Test
    void testVarParser() throws IOException {
        DisonJsonTransformer fixture = new DisonBuilder()
                .addFeature(new VarParser.Manager())
                .build();

        passEqualsTest(fixture, "simple-variable");
    }

    @Test
    void testIncludeAsChildrenParser() throws IOException {
        DisonJsonTransformer fixture = new DisonBuilder()
                .addFeature(new IncludeParser.Manager())
                .workingDirectory(workingDir)
                .build();

        passEqualsTest(fixture, "include-as-children-main");
    }

    @Test
    void testIncludeParser() throws IOException {
        DisonJsonTransformer fixture = new DisonBuilder()
                .addFeature(new IncludeParser.Manager())
                .workingDirectory(workingDir)
                .build();

        passEqualsTest(fixture, "include-main");
    }

    @Test
    void testIncludeParserWithVarMapping() throws IOException {
        DisonJsonTransformer fixture = new DisonBuilder()
                .addFeature(new IncludeParser.Manager())
                .addFeature(new VarParser.Manager())
                .workingDirectory(workingDir)
                .build();

        passEqualsTest(fixture, "include-with-var-main");
    }

    @Test
    void testAllPluginsTogetherBrrr() throws IOException {
        DisonJsonTransformer fixture = new DisonBuilder()
                .addAllFeatures()
                .workingDirectory(workingDir)
                .build();

        passEqualsTest(fixture, "mixed-test");
    }

    private void passEqualsTest(final DisonJsonTransformer fixture, final String filename) throws IOException {
        passEqualsTest(fixture, filename, a -> a);
    }

    private void passEqualsTest(final DisonJsonTransformer fixture, final String filename, Function<String, String> modifier) throws IOException {
        assertEquals(getResourceContent(filename.concat(".json")),
                modifier.apply(fixture.parse(getResourceContent(filename.concat(".dison")))));
    }

    private String getResourceContent(String filename) throws IOException {
        return new String(Objects.requireNonNull(LOADER.getResourceAsStream("parsing/".concat(filename)))
                .readAllBytes());
    }
}