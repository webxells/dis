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
package com.webxells.dis.json.config.map;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.MappingPoint;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.json.JsonException;
import com.webxells.dis.json.config.JsonMappingPart;
import com.webxells.dis.json.intern.Parser;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public abstract class MapEatingPart extends JsonMappingPart {
    protected int size;

    public MapEatingPart(final MappingConfiguration configuration) {
        super(configuration);
    }

    public MapEatingPart(final MappingConfiguration configuration, final MappingPoint input, final MappingPoint output) {
        super(configuration, input, output);
    }

    @Override
    public void validate() throws InvalidApi {
        if (getSubData().isEmpty()) {
            throw new InvalidApi("no use of map - subData required");
        }
    }

    protected abstract void saveEntry(final String title, final MappingPart mappingPart);

    @Override
    public void parseValue(final JsonReader jsonReader) throws IOException, JsonException {
        switch (jsonReader.peek()) {
            case BEGIN_OBJECT:
                readMap(jsonReader);
            case NULL:
                break;
            default:
                throw new JsonException("mapping requires an object");
        }
    }

    @Override
    public int valueSize() {
        return size;
    }

    private void readMap(final JsonReader jsonReader) throws IOException, JsonException {
        jsonReader.beginObject();
        while (JsonToken.NAME == jsonReader.peek()) {
            final String title = jsonReader.nextName();
            saveEntry(title, createMappingPart(jsonReader));
        }
        jsonReader.endObject();
    }

    private MappingPart createMappingPart(final JsonReader jsonReader) throws IOException, JsonException {
        final SimpleMappingPart result = new SimpleMappingPart(null);
        switch (jsonReader.peek()) {
            case BEGIN_ARRAY:
            case BEGIN_OBJECT:
                throw new UnsupportedEncodingException();
            case STRING:
            case NUMBER:
            case BOOLEAN:
            case NULL:
                result.getDataset().collect(new SimpleDatasetPiece(Parser.getCurrentScalarValue(jsonReader)));
                break;
            default:
                throw new JsonException("invalid json");
        }
        return result;
    }

    @Override
    @Required
    public void setSubData(final List<MappingConfiguration> subConfiguration) {
        super.setSubData(subConfiguration);
    }
}