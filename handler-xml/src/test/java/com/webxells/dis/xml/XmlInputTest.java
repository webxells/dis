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
package com.webxells.dis.xml;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.base.config.StableMappingPart;
import com.webxells.dis.test.TestResource;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XmlInputTest extends SimpleTestCase {

    @Test
    void testRootTagWithSubData() throws DisException {
        XmlConfig config = new XmlConfig();
        config.setName(random());
        config.setIterationPath("//root/main");
        config.setReceiver(new TestResource("subData.xml"));
        MappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(config.getName(), "//")
                        .addSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart()
                                        .setInput(config.getName(), "//sub")
                                )
                                .build())
                )
                .build();
        XmlInput fixture = new XmlInput(config);
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(0, fixture.read(mappingConfiguration));
        mappingConfiguration.clear();
        assertTrue(fixture.hasNext());
        assertEquals(0, fixture.read(mappingConfiguration));
        mappingConfiguration.clear();
        assertTrue(fixture.hasNext());
        assertEquals(1, fixture.read(mappingConfiguration));
        assertEquals("subtext", mappingConfiguration.parts().get(0).getSubData().get(0).parts().get(0).value().get());
        assertFalse(fixture.hasNext());
        fixture.end();
    }

    @Test
    void testWildcardNode() throws DisException {
        XmlConfig config = new XmlConfig();
        config.setName(random());
        config.setIterationPath("//root/*");
        config.setReceiver(new Resource() {
            @Override
            public String getType() {
                return null;
            }

            @Override
            public OutputStream send() {
                return null;
            }

            @Override
            public InputStream receive() {
                return XmlInputTest.class.getClassLoader().getResourceAsStream("wildcard.xml");
            }
        });

        SimpleMappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(createMappingPoint(config.getName(), "//child")))
                .build();
        XmlInput fixture = new XmlInput(config);
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(1, fixture.read(mappingConfiguration));
        assertEquals(1, mappingConfiguration.parts().get(0).getDataset().getContent().size());
        assertEquals("first-child", mappingConfiguration.parts().get(0).getDataset().getContent().get(0).value().get());
        mappingConfiguration.clear();
        assertTrue(fixture.hasNext());
        assertEquals(1, fixture.read(mappingConfiguration));
        assertEquals(1, mappingConfiguration.parts().get(0).getDataset().getContent().size());
        assertEquals("second-child", mappingConfiguration.parts().get(0).getDataset().getContent().get(0).value().get());
        assertFalse(fixture.hasNext());
        fixture.end();
    }

    @Test
    void testTmb() throws DisException {
        XmlConfig config = new XmlConfig();
        config.setName(random());
        config.setIterationPath("//root/poi");
        config.setRawReads(List.of("//description/text"));
        config.setReceiver(new Resource() {
            @Override
            public String getType() {
                return null;
            }

            @Override
            public OutputStream send() {
                return null;
            }

            @Override
            public InputStream receive() {
                return XmlInputTest.class.getClassLoader().getResourceAsStream("tmb.xml");
            }
        });

        SimpleMappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(createMappingPoint(config.getName(), "//connections/connection")))
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(createMappingPoint(config.getName(), "//description/text")))
                .build();
        SimpleMappingConfiguration connections = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(createMappingPoint(config.getName(), "//type")))
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(createMappingPoint(config.getName(), "//information")))
                .build();
        mappingConfiguration.parts().get(0).getSubData().add(connections);
        XmlInput fixture = new XmlInput(config);
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(11, fixture.read(mappingConfiguration));

        assertEquals(5, mappingConfiguration.parts().get(0).getSubData().size());

        assertFalse(fixture.hasNext());
        fixture.end();
    }

    @Test
    void testWithRawReads() throws DisException {
        String random1 = random();
        String random2 = random("unqiue value");
        String innerContent = String.format("<c attr1='%s'><workaround></workaround></c>" +
                                "<c><workaround>%s</workaround></c>",
                        random1, random2);
        ByteArrayInputStream byteArrayInputStream =
                new ByteArrayInputStream(("<a><b>" + innerContent + "</b></a>").getBytes());

        XmlConfig config = new XmlConfig();
        config.setName(random());
        config.setIterationPath("//a");
        config.setRawReads(List.of("//b"));
        config.setReceiver(new Resource() {
            @Override
            public String getType() {
                return null;
            }

            @Override
            public OutputStream send() {
                return null;
            }

            @Override
            public InputStream receive() {
                return byteArrayInputStream;
            }
        });
        SimpleMappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(createMappingPoint(config.getName(), "//b")))
                .build();

        XmlInput fixture = new XmlInput(config);
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(1, fixture.read(mappingConfiguration));
        assertEquals(innerContent, mappingConfiguration.parts().get(0).value().get());

        assertFalse(fixture.hasNext());
        fixture.end();
    }

    @Test
    void testWithRootAttributes() throws DisException {
        String attr11 = random("attr1");
        String attr12 = random("attr2");
        String attr21 = random("attr1");
        String attr22 = random("attr2");
        String attrSub1 = random("attr-sub1");
        String attrSub2 = random("attr-sub2");
        String contentSub1 = random("content-sub1");
        String contentSub2 = random("content-sub2");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                String.format("<a><b>" +
                                    "<c attr1='%s' attr2='%s'/>" +
                                    "<c attr1='%s' attr2='%s'></c>" +
                                    "<c><d attr='%s'>%s</d><d attr='%s'>%s</d></c>" +
                            "</b></a>",
                        attr11, attr12, attr21, attr22, attrSub1, contentSub1, attrSub2, contentSub2).getBytes());
        XmlConfig config = new XmlConfig();
        config.setName(random());
        config.setUnreachablePathStrategy(XmlConfig.UnreachablePathStrategy.NULL);
        config.setUnexpectedMultipleOccurrenceStrategy(XmlConfig.UnexpectedMultipleOccurrenceStrategy.SELECT_FIRST);
        config.setIterationPath("//a/b/c");
        config.setReceiver(new Resource() {
            @Override
            public String getType() {
                return null;
            }

            @Override
            public OutputStream send() {
                return null;
            }

            @Override
            public InputStream receive() {
                return byteArrayInputStream;
            }
        });
        SimpleMappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart().setInput(createMappingPoint(config.getName(), "//@attr1")))
                .addPart(ConfigurationBuilder.newPart().setInput(createMappingPoint(config.getName(), "//@attr2")))
                .addPart(ConfigurationBuilder.newPart().setInput(createMappingPoint(config.getName(), "//d")))
                .build();
        mappingConfiguration.parts().get(2).getSubData()
                .add(newConfiguration()
                        .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                                .setInput(createMappingPoint(config.getName(), "//@attr")))
                        .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                                .setInput(createMappingPoint(config.getName(), "//")))
                        .build());
        XmlInput fixture = new XmlInput(config);
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(2, fixture.read(mappingConfiguration));
        assertEquals(attr11, mappingConfiguration.parts().get(0).value().get());
        assertEquals(attr12, mappingConfiguration.parts().get(1).value().get());

        mappingConfiguration.clear();
        assertTrue(fixture.hasNext());
        assertEquals(2, fixture.read(mappingConfiguration));
        assertEquals(attr21, mappingConfiguration.parts().get(0).value().get());
        assertEquals(attr22, mappingConfiguration.parts().get(1).value().get());

        mappingConfiguration.clear();
        assertTrue(fixture.hasNext());
        assertEquals(4, fixture.read(mappingConfiguration));
        assertEquals(attrSub1, mappingConfiguration.parts().get(2).getSubData().get(0).parts().get(0).value().get());
        assertEquals(contentSub1, mappingConfiguration.parts().get(2).getSubData().get(0).parts().get(1).value().get());
        assertEquals(attrSub2, mappingConfiguration.parts().get(2).getSubData().get(1).parts().get(0).value().get());
        assertEquals(contentSub2, mappingConfiguration.parts().get(2).getSubData().get(1).parts().get(1).value().get());

        assertFalse(fixture.hasNext());
        fixture.end();
    }

    @Test
    void testWithVirtualRootTags() throws DisException {
        String v111 = random("v111");
        String v112attr = random("v112attr");
        String f1 = random("f1");
        String f2 = random("f2");
        String v211 = random("v211");
        String v213attr = random("v213attr");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                String.format("<a><b>" +
                                "<c><d><e>%s</e><b attr='%s'>%s</b></d><f>%s</f></c>\n" +
                                "<c><f>%s</f><d><e>%s</e><e /><b attr='%s'>%s</b></d></c>\r\n" +
                                "</b></a>",
                        v111, v112attr, random(), f1, f2, v211, v213attr, random()).getBytes());
        XmlConfig config = new XmlConfig();
        config.setName(random());
        config.setUnreachablePathStrategy(XmlConfig.UnreachablePathStrategy.NULL);
        config.setUnexpectedMultipleOccurrenceStrategy(XmlConfig.UnexpectedMultipleOccurrenceStrategy.SELECT_FIRST);
        config.setIterationPath("//a/b");
        config.setReceiver(new Resource() {
            @Override
            public String getType() {
                return null;
            }

            @Override
            public OutputStream send() {
                return null;
            }

            @Override
            public InputStream receive() {
                return byteArrayInputStream;
            }
        });
        SimpleMappingConfiguration mappingConfiguration = new SimpleMappingConfiguration();
        mappingConfiguration.setParts(List.of(
                new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint(config.getName(), "//c"),
                        new SimpleMappingPoint(null, null)
                )
        ));
        SimpleMappingConfiguration subMappingConfiguration = new SimpleMappingConfiguration();
        subMappingConfiguration.setParts(List.of(
                new StableMappingPart(subMappingConfiguration,
                        new SimpleMappingPoint(config.getName(), "//d/e"),
                        new SimpleMappingPoint(null, null)
                ),
                new StableMappingPart(subMappingConfiguration,
                        new SimpleMappingPoint(config.getName(), "//d/b@attr"),
                        new SimpleMappingPoint(null, null)
                ),
                new StableMappingPart(subMappingConfiguration,
                        new SimpleMappingPoint(config.getName(), "//f"),
                        new SimpleMappingPoint(null, null)
                )
        ));
        ((StableMappingPart) mappingConfiguration.parts().get(0)).setSubData(List.of(subMappingConfiguration));


        XmlInput fixture = new XmlInput(config);
        assertEquals(config.getName(), fixture.getName());
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(6, fixture.read(mappingConfiguration));
        MappingConfiguration round1subRound1 = mappingConfiguration.parts().get(0).getSubData().get(0);
        assertEquals(v111, round1subRound1.parts().get(0).value().get());
        assertEquals(v112attr, round1subRound1.parts().get(1).value().get());
        assertEquals(f1, round1subRound1.parts().get(2).value().get());
        MappingConfiguration round1subRound2 = mappingConfiguration.parts().get(0).getSubData().get(1);
        assertEquals(v211, round1subRound2.parts().get(0).value().get());
        assertEquals(v213attr, round1subRound2.parts().get(1).value().get());
        assertEquals(f2, round1subRound2.parts().get(2).value().get());

        assertFalse(fixture.hasNext());
        fixture.end();
    }

    @Test
    void testBigFieldFile() throws DisException {
        XmlConfig config = new XmlConfig();
        config.setName(random());
        config.setIterationPath("//a/b");
        config.setReceiver(new Resource() {
            @Override
            public String getType() {
                return null;
            }

            @Override
            public OutputStream send() {
                return null;
            }

            @Override
            public InputStream receive() {
                return XmlInputTest.class.getClassLoader().getResourceAsStream("bigField.xml");
            }
        });
        SimpleMappingConfiguration mappingConfiguration = new SimpleMappingConfiguration();
        mappingConfiguration.setParts(List.of(
                new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint(config.getName(), "//c"),
                        new SimpleMappingPoint(random(), "e1")
                )
        ));

        XmlInput fixture = new XmlInput(config);
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(1, fixture.read(mappingConfiguration));
        assertEquals(58859, mappingConfiguration.parts().get(0).value().get().length());

        assertFalse(fixture.hasNext());
        fixture.end();
    }

    @Test
    void withMultiOccurrence() throws DisException {
        String v111 = random("v111");
        String v112attr = random("v112attr");
        String v112 = random("v112");
        String v121 = random("v121");
        String v122attr = random("v122attr");
        String v122 = random("v122");
        String f1 = random("f1");
        String f2 = random("f2");
        String v211 = random("v211");
        String v213attr = random("v213attr");
        String v213 = random("v213");
        String v221 = random("v221");
        String v223attr = random("v223atr");
        String v223 = random("v223");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                String.format("<a><b>" +
                                "<c><d><e>%s</e><e attr='%s'>%s</e></d><d><e>%s</e><e attr='%s'>%s</e></d><f>%s</f></c>\n" +
                                "<c><f>%s</f><d><e>%s</e><e /><e attr='%s'>%s</e></d><d><e>%s</e><e /><e attr='%s'>%s</e></d></c>\r\n" +
                            "</b></a>",
                        v111, v112attr, v112, v121, v122attr, v122, f1, f2, v211, v213attr, v213, v221, v223attr,
                        v223).getBytes());
        XmlConfig config = new XmlConfig();
        config.setName(random());
        config.setUnreachablePathStrategy(XmlConfig.UnreachablePathStrategy.NULL);
        config.setUnexpectedMultipleOccurrenceStrategy(XmlConfig.UnexpectedMultipleOccurrenceStrategy.SELECT_FIRST);
        config.setIterationPath("//a/b/c");
        config.setReceiver(new Resource() {
            @Override
            public String getType() {
                return null;
            }

            @Override
            public OutputStream send() {
                return null;
            }

            @Override
            public InputStream receive() {
                return byteArrayInputStream;
            }
        });
        SimpleMappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .setInput(createMappingPoint(config.getName(), "//d")))
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .setInput(createMappingPoint(config.getName(), "//f")))
                .build();

        SimpleMappingConfiguration subMappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .setInput(createMappingPoint(config.getName(), "//e")))
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .setInput(createMappingPoint(config.getName(), "//e@attr")))
                .build();
        ((StableMappingPart) mappingConfiguration.parts().get(0)).setSubData(List.of(subMappingConfiguration));


        XmlInput fixture = new XmlInput(config);
        assertEquals(config.getName(), fixture.getName());
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(5, fixture.read(mappingConfiguration));
        MappingConfiguration round1subRound1 = mappingConfiguration.parts().get(0).getSubData().get(0);
        assertEquals(f1, mappingConfiguration.parts().get(1).value().get());
        assertEquals(v111, round1subRound1.parts().get(0).getDataset().getContent().get(0).value().get());
        assertEquals(v112attr, round1subRound1.parts().get(1).getDataset().getContent().get(0).value().get());
        MappingConfiguration round1subRound2 = mappingConfiguration.parts().get(0).getSubData().get(1);
        assertEquals(v121, round1subRound2.parts().get(0).getDataset().getContent().get(0).value().get());
        assertEquals(v122attr, round1subRound2.parts().get(1).getDataset().getContent().get(0).value().get());
        mappingConfiguration.clear();
        assertEquals(5, fixture.read(mappingConfiguration));
        MappingConfiguration round2subRound1 = mappingConfiguration.parts().get(0).getSubData().get(0);
        assertEquals(f2, mappingConfiguration.parts().get(1).value().get());
        assertEquals(v211, round2subRound1.parts().get(0).getDataset().getContent().get(0).value().get());
        assertEquals(v213attr, round2subRound1.parts().get(1).getDataset().getContent().get(0).value().get());
        MappingConfiguration round2subRound2 = mappingConfiguration.parts().get(0).getSubData().get(1);
        assertEquals(v221, round2subRound2.parts().get(0).getDataset().getContent().get(0).value().get());
        assertEquals(v223attr, round2subRound2.parts().get(1).getDataset().getContent().get(0).value().get());

        assertFalse(fixture.hasNext());
        fixture.end();
    }

    @Test
    void WithUnreachablePathStrategyFail() throws DisException {
        String e1 = random("e1");
        String e2 = random("e2");
        String e3 = random("e3");
        assertThrows(InputOutputError.class, () -> test(e1, e2, e3, e1, e3,
                XmlConfig.UnexpectedMultipleOccurrenceStrategy.SELECT_FIRST, XmlConfig.UnreachablePathStrategy.FAIL));
    }


    @Test
    void  WithUnreachablePathStrategyNull() throws DisException {
        String e1 = random("e1");
        String e2 = random("e2");
        String e3 = random("e3");
        test(e1, e2, e3, e1, e3, XmlConfig.UnexpectedMultipleOccurrenceStrategy.SELECT_FIRST,
                XmlConfig.UnreachablePathStrategy.NULL);
    }

    @Test
    void WithOccurrenceStrategyFail() throws DisException {
        String e1 = random("e1");
        String e2 = random("e2");
        String e3 = random("e3");
        assertThrows(InputOutputError.class, () -> test(e1, e2, e3, e1, e3,
                XmlConfig.UnexpectedMultipleOccurrenceStrategy.FAIL, XmlConfig.UnreachablePathStrategy.EMPTY));
    }

    @Test
    void testWithOccurrenceStrategyLast() throws DisException {
        String e1 = random("e1");
        String e2 = random("e2");
        String e3 = random("e3");
        test(e1, e2, e3, e2, "",  XmlConfig.UnexpectedMultipleOccurrenceStrategy.SELECT_LAST, XmlConfig.UnreachablePathStrategy.EMPTY);
    }

    @Test
    void testWithOccurrenceStrategyFirst() throws DisException {
        String e1 = random("e1");
        String e2 = random("e2");
        String e3 = random("e3");
        test(e1, e2, e3, e1, e3, XmlConfig.UnexpectedMultipleOccurrenceStrategy.SELECT_FIRST, XmlConfig.UnreachablePathStrategy.EMPTY);
    }

    void test(String e1, String e2, String e3, String e11AssertValue, String e21AssertValue,
              XmlConfig.UnexpectedMultipleOccurrenceStrategy unexpectedMultipleOccurrenceStrategy,
              final XmlConfig.UnreachablePathStrategy unreachablePathStrategy) throws DisException {
        String unreachableValue = XmlConfig.UnreachablePathStrategy.NULL.equals(unreachablePathStrategy) ? null : "";
        String a1 = random("a1");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
               String.format("<a><b>" +
                       "<c><d><e>%s</e><e attr='%s'>%s</e></d></c>\n" +
                       "<c><d><e>%s</e><e /></d></c>\r\n" +
                       "</b></a>", e1, a1, e2, e3).getBytes());
        XmlConfig config = new XmlConfig();
        config.setName(random());
        config.setUnexpectedMultipleOccurrenceStrategy(unexpectedMultipleOccurrenceStrategy);
        config.setUnreachablePathStrategy(unreachablePathStrategy);
        config.setIterationPath("//a/b/c");
        config.setReceiver(new Resource() {
            @Override
            public String getType() {
                return null;
            }

            @Override
            public OutputStream send() throws InputOutputError {
                return null;
            }

            @Override
            public InputStream receive() throws InputOutputError {
                return byteArrayInputStream;
            }
        });
        SimpleMappingConfiguration mappingConfiguration = new SimpleMappingConfiguration();
        mappingConfiguration.setParts(List.of(
                new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint(config.getName(), "//d/e"),
                        new SimpleMappingPoint(random(), "e1")
                ),
                new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint(config.getName(), "//d/e[2]@attr"),
                        new SimpleMappingPoint(random(), "a1")
                ),
                new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint(config.getName(), "//d/e[@attr='"+a1+"']"),
                        new SimpleMappingPoint(random(), "e2")
                )
        ));

        XmlInput fixture = new XmlInput(config);
        assertEquals(config.getName(), fixture.getName());
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(3, fixture.read(mappingConfiguration));


        assertEquals(e11AssertValue, mappingConfiguration.parts().get(0).getDataset().getContent().get(0).value().get());
        assertEquals(a1, mappingConfiguration.parts().get(1).getDataset().getContent().get(0).value().get());
        assertEquals(e2, mappingConfiguration.parts().get(2).getDataset().getContent().get(0).value().get());

        assertTrue(fixture.hasNext());

        mappingConfiguration.clear();

        int result =  fixture.read(mappingConfiguration);
        assertEquals(e21AssertValue,
                mappingConfiguration.parts().get(0).getDataset().getContent().get(0).value().get());
        if (XmlConfig.UnreachablePathStrategy.NULL.equals(unreachablePathStrategy)) {
            assertEquals(1, result);
            assertTrue(mappingConfiguration.parts().get(1).getDataset().getContent().isEmpty());
            assertTrue(mappingConfiguration.parts().get(2).getDataset().getContent().isEmpty());
        } else {
            assertEquals(3, result);
            assertEquals(unreachableValue, mappingConfiguration.parts().get(1).getDataset().getContent().get(0).value().get());
            assertEquals(unreachableValue, mappingConfiguration.parts().get(2).getDataset().getContent().get(0).value().get());
        }


        assertFalse(fixture.hasNext());
        fixture.end();
    }

}