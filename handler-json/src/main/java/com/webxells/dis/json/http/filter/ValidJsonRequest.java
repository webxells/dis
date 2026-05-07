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
package com.webxells.dis.json.http.filter;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.rest.Request;
import com.webxells.dis.api.rest.filter.RequestFilter;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.gson.stream.JsonToken.BEGIN_ARRAY;
import static com.google.gson.stream.JsonToken.BEGIN_OBJECT;
import static com.google.gson.stream.JsonToken.END_DOCUMENT;

@Description("RequestFilter asserts valid json send")
public class ValidJsonRequest implements RequestFilter {
    public enum Token {
        OBJECT(BEGIN_OBJECT), ARRAY(BEGIN_ARRAY), NULL(JsonToken.NULL),
        STRING(JsonToken.STRING), NUMBER(JsonToken.NUMBER), BOOLEAN(JsonToken.BOOLEAN),
        ANY(null);

        private final JsonToken next;

        Token(final JsonToken next) {
            this.next = next;
        }


        public JsonToken getNext() {
            return next;
        }
    }

    private static final Logger LOGGER = LoggerProxyFactory.logger(ValidJsonRequest.class);

    @Description("Mapping of required paths and types")
    private Map<String, Token> requiredPaths = Map.of();

    @Override
    public Optional<ReturnState> resolve(final Request request) {
        try(final InputStream body = request.getBody()) {
            final boolean result = invalidJson(body);
            return result ? Optional.of(ReturnState.NOT_ACCEPTABLE) : Optional.empty();
        } catch (final IOException e) {
            throw new RuntimeException("could not close body", e);
        }
    }

    private boolean invalidJson(final InputStream body) {
        final Set<String> foundPaths = new HashSet<>();
        try {
            final JsonReader jsonReader = new JsonReader(new InputStreamReader(body));
            while (END_DOCUMENT != jsonReader.peek()) {
              switch (jsonReader.peek()) {
                  case BEGIN_ARRAY:
                      jsonReader.beginArray();
                      break;
                  case END_ARRAY:
                      jsonReader.endArray();
                      break;
                  case BEGIN_OBJECT:
                      jsonReader.beginObject();
                      break;
                  case END_OBJECT:
                      jsonReader.endObject();
                      break;
                  case NAME:
                      jsonReader.nextName();
                      assertNoMissingRequiredPath(jsonReader, foundPaths);
                      break;
                  case STRING:
                  case NUMBER:
                  case BOOLEAN:
                  case NULL:
                      assertNoMissingRequiredPath(jsonReader, foundPaths);
                      jsonReader.skipValue();
              }
            }
        } catch (final Throwable ignored) {
            LOGGER.d("Invalid json payload");
            return true;
        }
        if(foundPaths.size() != requiredPaths.size()) {
            final String errorsPaths = requiredPaths.keySet().stream()
                            .filter(a -> !foundPaths.contains(a))
                            .collect(Collectors.joining(","));
            LOGGER.debug("Some requiredPaths not found: ".concat(errorsPaths));
            return true;
        }
        return false;
    }

    private void assertNoMissingRequiredPath(final JsonReader jsonReader, final Set<String> foundPaths) throws IOException {
        final String path = jsonReader.getPath();
        final Token requiredToken = requiredPaths.get(path);
        if (null != requiredToken) {
            final JsonToken token = jsonReader.peek();
            if (Token.ANY != requiredToken && requiredToken.getNext() != token) {
                final String msg = String.format("requiredPath[%s] is not %s but %s", path, requiredToken.getNext(), token);
                LOGGER.trace(msg);
                throw new IOException(msg);
            }
            foundPaths.add(path);
        }
    }

    public void setRequiredPaths(final Map<String, Token> requiredPaths) {
        this.requiredPaths = requiredPaths;
    }
}