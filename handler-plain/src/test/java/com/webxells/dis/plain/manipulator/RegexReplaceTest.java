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

import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.plain.Option;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegexReplaceTest {
    @Test
    void regex() throws InvalidDatasetException {
        test("aaaabaaaa", "a{3}", Set.of(), "b", "babba");
        test("aaaa\nbaaaa", "^.*b.+$",  Set.of(Option.DOTALL), "b", "b");
        test("aa\naa\nbaa\naa", "^.*aa.*$",  Set.of(Option.MULTILINE), "b", "b\nb\nb\nb");
    }

    @Test
    void testMappingParts() throws InvalidDatasetException {
        SimpleMappingConfiguration configuration = new SimpleMappingConfiguration();

        SimpleMappingPart searchMappingPart = new SimpleMappingPart(configuration, new SimpleMappingPoint("dev/null","search"), new SimpleMappingPoint());
        searchMappingPart.getDataset().getContent().add(new SimpleDatasetPiece("a{3}"));
        SimpleMappingPart replaceMappingPart = new SimpleMappingPart(configuration, new SimpleMappingPoint("dev/null", "replace"), new SimpleMappingPoint());
        replaceMappingPart.getDataset().getContent().add(new SimpleDatasetPiece("b"));

        configuration.setParts(List.of(searchMappingPart, replaceMappingPart));

        test("aaaabaaaa", new SimpleMappingPortrayal(MappingPortrayal.Source.INPUT, "dev/null", "search"), Set.of(),
                new SimpleMappingPortrayal(MappingPortrayal.Source.INPUT, "dev/null", "replace"), "babba", searchMappingPart);
    }

    private void test(String oldValue, String search, Set<Option> options, String replace, String newValue) throws InvalidDatasetException {
        RegexReplace fixture = new RegexReplace();
        fixture.setSearch(search);
        fixture.setOptions(new LinkedList<>(options));
        fixture.setReplace(replace);

        SimpleDatasetPiece piece = new SimpleDatasetPiece(oldValue);
        fixture.manipulate(piece, new SimpleMappingPart(null));

        assertEquals(newValue, piece.value().get());
    }

    private void test(final String oldValue, final MappingPortrayal search, final Set<Option> options, final MappingPortrayal replace, final String newValue, final MappingPart mappingPart) throws InvalidDatasetException {
        RegexReplace fixture = new RegexReplace();
        fixture.setSearchByMappingPortrayal(search);
        fixture.setOptions(new LinkedList<>(options));
        fixture.setReplaceByMappingPortrayal(replace);

        SimpleDatasetPiece piece = new SimpleDatasetPiece(oldValue);
        fixture.manipulate(piece, mappingPart);

        assertEquals(newValue, piece.value().get());
    }

}