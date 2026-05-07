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
package com.webxells.dis.plain;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.base.config.StableMappingPart;
import com.webxells.dis.plain.output.Echo;
import com.webxells.dis.plain.output.EchoConfig;
import com.webxells.dis.plain.output.parser.SyntaxError;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static com.webxells.dis.test.cases.ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EchoTest extends SimpleTestCase {

    @Test
    void testEvaluateVariableNames() throws DisException {
        final String content = random();
        SimpleMappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(RANDOM)
                        .setOutput(new SimpleMappingPoint("test", "variable1"))
                        .withContent("variable2")
                ).addPart(ConfigurationBuilder.newPart(RANDOM)
                        .setOutput(new SimpleMappingPoint("test", "variable2"))
                        .withContent(content)
                )
                .build();
        EchoConfig config = new EchoConfig();
        config.setName(mappingConfiguration.parts().get(0).getOutput().getReference());
        config.setTemplate("$$variable1");
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        config.setSender(new Resource() {
            @Override
            public OutputStream send() {
                return output;
            }

            @Override
            public InputStream receive() {
                return null;
            }

            @Override
            public String getType() {
                return null;
            }
        });
        Echo fixture = new Echo(config);
        fixture.start();
        fixture.write(mappingConfiguration);
        fixture.end();
        assertEquals("$variable2", output.toString());
        output.reset();
        config.setEvaluateVarNames(true);
        fixture = new Echo(config);
        fixture.start();
        fixture.write(mappingConfiguration);
        fixture.end();
        assertEquals(content, output.toString());

        output.reset();
        config.setTemplate("\"$variable$variable1\"");
        mappingConfiguration.parts().get(0).getDataset().getContent().get(0).rewriteValue("2");
        fixture = new Echo(config);
        fixture.start();
        fixture.write(mappingConfiguration);
        fixture.end();
        assertEquals(String.format("\"%s\"", content), output.toString());

    }

    @Test
    void testFreakyVariableNames() throws DisException {
        SimpleMappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(RANDOM)
                        .setOutput(new SimpleMappingPoint("test", "freaky[0]"))
                        .withContent()
                )
                .build();
        EchoConfig config = new EchoConfig();
        config.setName(mappingConfiguration.parts().get(0).getOutput().getReference());
        config.setTemplate("test${freaky[0]}");
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        config.setSender(new Resource() {
            @Override
            public OutputStream send() {
                return output;
            }

            @Override
            public InputStream receive() {
                return null;
            }

            @Override
            public String getType() {
                return null;
            }
        });
        Echo fixture = new Echo(config);
        fixture.start();
        fixture.write(mappingConfiguration);
        fixture.end();
        assertEquals(String.format("test%s", mappingConfiguration.parts().get(0).value().get()), output.toString());
    }

    @Test
    void testDontShowUnmatchedVariables() throws DisException {
        SimpleMappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(RANDOM)
                        .setOutput(new SimpleMappingPoint("test", "var"))
                        .withContent()
                )
                .build();
        EchoConfig config = new EchoConfig();
        config.setName("test");
        config.setTemplate("$var $unknown");
        config.setHideUnknownVariables(true);
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        config.setSender(new Resource() {
            @Override
            public OutputStream send() {
                return output;
            }

            @Override
            public InputStream receive() {
                return null;
            }

            @Override
            public String getType() {
                return null;
            }
        });
        Echo fixture = new Echo(config);
        fixture.start();
        fixture.write(mappingConfiguration);
        fixture.end();
        assertEquals(String.format("%s ", mappingConfiguration.parts().get(0).value().get()), output.toString());
    }

    @Test
    void badSyntax() throws DisException {
        shouldFailByBadSyntax("zh$asdzh${test{}");
        shouldFailByBadSyntax("a${test");
        shouldFailByBadSyntax("a*{test");
    }

    private void shouldFailByBadSyntax(String template) throws DisException {
        EchoConfig config = new EchoConfig();
        config.setName(random());
        config.setTemplate(template);
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        config.setSender(new Resource() {
            @Override
            public OutputStream send() {
                return output;
            }

            @Override
            public InputStream receive() {
                return null;
            }

            @Override
            public String getType() {
                return null;
            }
        });
        Echo fixture = new Echo(config);
        fixture.start();
        assertThrows(SyntaxError.class, () -> fixture.write(newConfiguration().build()));
    }

    @Test
    void valueSource() throws DisException {
        String value1 = random("value1");
        String value2 = random("value2");
        String value3 = random("value3");

        SimpleMappingConfiguration mappingConfiguration = new SimpleMappingConfiguration();
        mappingConfiguration.setParts(List.of(
                new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint("test", "first"),
                        new SimpleMappingPoint("test", "second")) {{
                    getDataset().collect(new SimpleDatasetPiece(value1));
                }}, new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint("test", "second"),
                        new SimpleMappingPoint("test", "first")) {{
                    getDataset().collect(new SimpleDatasetPiece(value2));
                }}
        ));
        assertEquals(String.format("%s %s", value1, value2), test(mappingConfiguration, EchoConfig.ValueSource.INPUT));
        assertEquals(String.format("%s %s", value2, value1), test(mappingConfiguration, EchoConfig.ValueSource.OUTPUT));
        assertEquals(String.format("%s %s", value2, value1), test(mappingConfiguration, EchoConfig.ValueSource.BOTH));

        assertEquals(String.format("%s %s", value1, value2), test(mappingConfiguration,
                EchoConfig.ValueSource.INPUT_OUTPUT));
        assertEquals(String.format("%s %s", value2, value1), test(mappingConfiguration,
                EchoConfig.ValueSource.OUTPUT_INPUT));

        mappingConfiguration = new SimpleMappingConfiguration();
        mappingConfiguration.setParts(List.of(
                new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint(),
                        new SimpleMappingPoint("test", "first")) {{
                    getDataset().collect(new SimpleDatasetPiece(value1));
                }}, new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint("test", "second"),
                        new SimpleMappingPoint()) {{
                    getDataset().collect(new SimpleDatasetPiece(value2));
                }}
        ));
        assertEquals(String.format("%s %s", value1, value2), test(mappingConfiguration,
                EchoConfig.ValueSource.INPUT_OUTPUT));
        assertEquals(String.format("%s %s", value1, value2), test(mappingConfiguration,
                EchoConfig.ValueSource.OUTPUT_INPUT));

        assertEquals(String.format("$first %s", value2), test(mappingConfiguration,
                EchoConfig.ValueSource.INPUT));
        assertEquals(String.format("%s $second", value1), test(mappingConfiguration,
                EchoConfig.ValueSource.OUTPUT));
        assertEquals(String.format("%s %s", value1, value2), test(mappingConfiguration,
                EchoConfig.ValueSource.BOTH));


        mappingConfiguration = new SimpleMappingConfiguration();
        mappingConfiguration.setParts(List.of(
                new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint(),
                        new SimpleMappingPoint("test", "first")) {{
                    getDataset().collect(new SimpleDatasetPiece(value1));
                }}, new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint("test", "second"),
                        new SimpleMappingPoint()) {{
                    getDataset().collect(new SimpleDatasetPiece(value2));
                }}, new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint("test", "first"),
                        new SimpleMappingPoint("test", "second")) {{
                    getDataset().collect(new SimpleDatasetPiece(value3));
                }}
        ));

        assertEquals(String.format("%s %s", value3, value2), test(mappingConfiguration,
                EchoConfig.ValueSource.INPUT_OUTPUT));
        assertEquals(String.format("%s %s", value1, value3), test(mappingConfiguration,
                EchoConfig.ValueSource.OUTPUT_INPUT));

        assertEquals(String.format("%s %s", value3, value2), test(mappingConfiguration,
                EchoConfig.ValueSource.INPUT));
        assertEquals(String.format("%s %s", value1, value3), test(mappingConfiguration,
                EchoConfig.ValueSource.OUTPUT));
        assertEquals(String.format("%s %s", value1, value2), test(mappingConfiguration,
                EchoConfig.ValueSource.BOTH));
    }

    @Test
    void cloneWithDifferentName() {
        EchoConfig fixture = new EchoConfig();
        fixture.setTemplate(random());
        fixture.setName("FIRST NAME");
        fixture.setCharsToEscape(List.of('a','b'));

        final EchoConfig actual = fixture.cloneWithDifferentName("SECOND NAME");

        assertEquals(fixture.getTemplate(), actual.getTemplate());
        assertEquals("SECOND NAME", actual.getName());
        assertEquals(fixture.getCharsToEscape(), actual.getCharsToEscape());
    }

    private String test(MappingConfiguration mappingConfiguration, EchoConfig.ValueSource valueSource) throws DisException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        EchoConfig config = new EchoConfig();
        config.setTemplate("$first $second");
        config.setSender(new Resource() {
            @Override
            public OutputStream send() {
                return output;
            }

            @Override
            public InputStream receive() {
                return null;
            }

            @Override
            public String getType() {
                return null;
            }
        });
        config.setName("test");
        config.setValueSource(valueSource);
        Echo fixture = new Echo(config);
        fixture.start();
        fixture.write(mappingConfiguration);
        fixture.end();
        return output.toString();
    }

    @Test
    void escapeFun() throws DisException {
        SimpleMappingConfiguration mappingConfiguration = new SimpleMappingConfiguration();
        mappingConfiguration.setParts(List.of(
                new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint("test", "first"),
                        new SimpleMappingPoint("test", "first")) {{
                    getDataset().collect(new SimpleDatasetPiece("\\\"asdasd\"sadasd"));
                }}, new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint("test", "second"),
                        new SimpleMappingPoint("test", "second")) {{
                    getDataset().collect(new SimpleDatasetPiece("\"asdasd\\\\\\\"sadasd"));
                }}));

        EchoConfig config = new EchoConfig();
        config.setName("test");
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        config.setSender(new Resource() {
            @Override
            public OutputStream send() {
                return output;
            }

            @Override
            public InputStream receive() {
                return null;
            }

            @Override
            public String getType() {
                return null;
            }
        });
        config.setTemplate("first: \"$first\", second: \"$second\"");
        Echo fixture = new Echo(config);
        fixture.start();
        fixture.write(mappingConfiguration);
        assertEquals("first: \"\\\"asdasd\"sadasd\", second: \"\"asdasd\\\\\\\"sadasd\"", output.toString());
        fixture.end();

        final ByteArrayOutputStream output2 = new ByteArrayOutputStream();
        config = new EchoConfig();
        config.setName("test");
        config.setCharsToEscape(List.of('"', 'd'));
        config.setSender(new Resource() {
            @Override
            public OutputStream send() {
                return output2;
            }

            @Override
            public InputStream receive() {
                return null;
            }

            @Override
            public String getType() {
                return null;
            }
        });
        config.setTemplate("first: \"$first\", second: \"$second\"");
        fixture = new Echo(config);
        fixture.start();
        fixture.write(mappingConfiguration);
        assertEquals(
                "first: \"\\\\\"as\\das\\d\\\"sa\\das\\d\", second: \"\\\"as\\das\\d\\\\\\\\\"sa\\das\\d\"",
                output2.toString());
        fixture.end();
    }


    @Test
    void notThatExtremeOutput() throws DisException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String resource = random("resource");
        SimpleMappingConfiguration mappingConfiguration = newConfiguration()
                .setFollowingOutputResource(resource)
                .addPart(
                        new ConfigurationBuilder.PartBuilder(RANDOM)
                                .withContent(random("first"))
                )
                .addPart(
                        new ConfigurationBuilder.PartBuilder(RANDOM)
                                .withContent(random("second"))
                )
                .addPart(
                        new ConfigurationBuilder.PartBuilder(RANDOM)
                                .addSubData(
                                        newConfiguration()
                                                .setFollowingOutputResource(resource)
                                                .addPart(
                                                        new ConfigurationBuilder.PartBuilder(RANDOM)
                                                                .withContent(random("sub1"))
                                                )
                                                .addPart(
                                                        new ConfigurationBuilder.PartBuilder(RANDOM)
                                                                .withContent(random("subsub11"), random("subsub12"),
                                                                        random("subsub13"))
                                                )
                                                .build()
                                )
                                .addSameSubData(
                                        newConfiguration()
                                                .addPart(
                                                        new ConfigurationBuilder.PartBuilder(RANDOM)
                                                                .withContent(random("sub2"))
                                                )
                                                .addPart(
                                                        new ConfigurationBuilder.PartBuilder(RANDOM)
                                                                .withContent(random("subsub21"), random("subsub22"),
                                                                        random("subsub23"))
                                                )
                                                .build()
                                )
                )
                .build();

        EchoConfig config = new EchoConfig();
        config.setName(resource);
        config.setSender(new Resource() {
            @Override
            public OutputStream send() {
                return output;
            }

            @Override
            public InputStream receive() {
                return null;
            }

            @Override
            public String getType() {
                return null;
            }
        });
        config.setTemplate(String.format(
                "$%1$s $unknown *unknown $%1$s *%3$s $%2$s",
                mappingConfiguration.parts().get(0).getOutput().getPath(),
                mappingConfiguration.parts().get(1).getOutput().getPath(),
                mappingConfiguration.parts().get(2).getOutput().getPath()
        ));
        String randomStatic = random("static");
        String mappingName1 = mappingConfiguration.parts().get(2).getOutput().getPath();
        String mappingName2 = mappingConfiguration.parts().get(2).getSubData()
                .get(0).parts().get(1).getOutput().getPath();
        config.setMultiMapping(Map.of(
                mappingName1, new MultiMapping() {{
                    setAfter(random("sub-after"));
                    setBefore(random("sub-before"));
                    setDelimiter(random("sub-delimiter"));
                    setTemplate(String.format("%s$%s *%s", randomStatic,
                            mappingConfiguration.parts().get(2).getSubData().get(0).parts()
                                    .get(0).getOutput().getPath(),
                            mappingConfiguration.parts().get(2).getSubData().get(0).parts().get(1).getOutput().getPath()));
                    setSourceType(SourceType.MULTI_CONFIGURATION);
                }},
                mappingName2, new MultiMapping() {{
                    setAfter(random("sub2-after"));
                    setBefore(random("subs2-before"));
                    setDelimiter(random("sub2-delimiter"));
                    setSourceType(SourceType.MULTI_VALUE);
                }}
        ));
        Echo fixture = new Echo(config);
        fixture.start();
        fixture.write(mappingConfiguration);
        String multiMapping21 = String.format("%s%s%s%s%s%s%s",
                config.getMultiMapping().get(mappingName2).getBefore(),
                mappingConfiguration.parts().get(2).getSubData().get(0).parts().get(1).getDataset().getContent().get(0).value().get(),
                config.getMultiMapping().get(mappingName2).getDelimiter(),
                mappingConfiguration.parts().get(2).getSubData().get(0).parts().get(1).getDataset().getContent().get(1).value().get(),
                config.getMultiMapping().get(mappingName2).getDelimiter(),
                mappingConfiguration.parts().get(2).getSubData().get(0).parts().get(1).getDataset().getContent().get(2).value().get(),
                config.getMultiMapping().get(mappingName2).getAfter()
        );
        String multiMapping22 = String.format("%s%s%s%s%s%s%s",
                config.getMultiMapping().get(mappingName2).getBefore(),
                mappingConfiguration.parts().get(2).getSubData().get(1).parts().get(1).getDataset().getContent().get(0).value().get(),
                config.getMultiMapping().get(mappingName2).getDelimiter(),
                mappingConfiguration.parts().get(2).getSubData().get(1).parts().get(1).getDataset().getContent().get(1).value().get(),
                config.getMultiMapping().get(mappingName2).getDelimiter(),
                mappingConfiguration.parts().get(2).getSubData().get(1).parts().get(1).getDataset().getContent().get(2).value().get(),
                config.getMultiMapping().get(mappingName2).getAfter()
        );
        String multiMapping = String.format("%s%s%s %s%s%s%s %s%s",
                config.getMultiMapping().get(mappingName1).getBefore(),
                randomStatic,
                mappingConfiguration.parts().get(2).getSubData().get(0).parts().get(0).value().get(),
                multiMapping21,
                config.getMultiMapping().get(mappingName1).getDelimiter(),
                randomStatic,
                mappingConfiguration.parts().get(2).getSubData().get(1).parts().get(0).value().get(),
                multiMapping22,
                config.getMultiMapping().get(mappingName1).getAfter()
        );
        assertEquals(String.format("%s $unknown *unknown %s %s %s",
                mappingConfiguration.parts().get(0).value().get(),
                mappingConfiguration.parts().get(0).value().get(),
                multiMapping,
                mappingConfiguration.parts().get(1).value().get()), output.toString());
        fixture.end();
    }

}