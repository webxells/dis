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
package com.webxells.dis.rest.cache;

import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.hash.Engine;
import com.webxells.dis.rest.cache.intern.RemnantReaderInputStream;
import com.webxells.dis.rest.cache.intern.SavingByteArrayOutputStream;
import com.webxells.dis.rest.execution.BodyPublisherOfInputStream;
import com.webxells.dis.rest.execution.Headers;
import com.webxells.dis.rest.execution.Request;
import com.webxells.dis.rest.execution.Response;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileCache implements CacheStrategy {
    private class FileEntity implements CacheEntity {
        private static final String SEPARATOR = "--__--";

        private final Path path;
        private final Request request;
        private final SavingByteArrayOutputStream requestBody;
        private final String dataSeparator;

        public FileEntity(final Request request) throws IOException {
            this.request = request;
            requestBody = new SavingByteArrayOutputStream(request.getBodyPublisher());
            path = createPath();
            dataSeparator = String.format("%s%s%s",
                    this.path.toFile().getName(), SEPARATOR, this.getClass().getSimpleName());
        }

        @Override
        public boolean requiresRevalidation() throws IOException {
            return !Files.isRegularFile(path) || (null != fileDateCacheCycle && fileDateCacheCycle.revalidate(path));
        }

        @Override
        public void set(final Response response) throws IOException {
            try (final BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(path.toFile()));
                 final InputStream body = response.body()) {
                output.write(String.valueOf(response.getStatusCode()).getBytes(StandardCharsets.UTF_8));
                output.write('\n');
                final Headers headers = response.getHeaders();
                for (final String key : headers.keys()) {
                    for (final String value : headers.get(key)) {
                        output.write(String.format("%s:%s", key, value).getBytes(StandardCharsets.UTF_8));
                        output.write('\n');
                    }
                }
                output.write(dataSeparator.getBytes(StandardCharsets.UTF_8));
                output.write('\n');
                body.transferTo(output);
                output.flush();
            }
        }

        @Override
        public Response response() throws IOException {
            final BufferedReader reader = new BufferedReader(new FileReader(path.toFile()));
            return new Response(
                    statusCode(reader.readLine()),
                    headers(reader),
                    request.getUri(),
                    new RemnantReaderInputStream(reader)
            );
        }

        private Headers headers(final BufferedReader reader) throws IOException {
            String current;
            final Headers headers = new Headers();
            while ((current = reader.readLine()) != null) {
                if (current.equals(dataSeparator)) {
                    return headers;
                }
                final int posSeparator = current.indexOf(':');
                if (posSeparator == -1) {
                    throw new IOException("Corrupt cache file: " + path);
                }
                headers.add(current.substring(0, posSeparator), current.substring(posSeparator + 1));
            }
            throw new IOException("Corrupt cache file: " + path);
        }

        private int statusCode(final String s) throws IOException {
            if (s.codePoints().anyMatch(Character::isDigit)) {
                return Integer.parseInt(s);
            }
            throw new IOException("Invalid cache file: invalid status code: " + s);
        }

        @Override
        public Request request() {
            return request.copy(new BodyPublisherOfInputStream(() -> new ByteArrayInputStream(requestBody.getInternal()),
                    requestBody.size()));
        }

        private Path createPath() {
            return cacheDir.resolve(engine.convert(createHashString()));
        }

        private String createHashString() {
            final StringBuilder toHash = new StringBuilder();
            toHash.append(request.getMethod().name());
            toHash.append(SEPARATOR);
            toHash.append(request.getUri().toString());
            toHash.append(SEPARATOR);
            request.getHeaders().stream()
                    .map(h -> String.format("%s%s%s", h.getKey(), SEPARATOR, h.getValue()))
                    .forEach(toHash::append);
            toHash.append(SEPARATOR);
            toHash.append(engine.convert(requestBody.toString()));
            return toHash.toString();
        }
    }

    private Path cacheDir;
    private Engine engine;
    private FileDateCacheCycle fileDateCacheCycle;

    @Override
    public void validate() throws InvalidApi {
        if (null == engine || null == cacheDir) {
            throw new InvalidApi("required fields missing");
        }
        if (!(Files.isDirectory(cacheDir) && Files.isWritable(cacheDir))) {
            throw new InvalidApi("invalid cache dir: " + cacheDir);
        }
    }

    @Override
    public CacheEntity request(final Request request) throws IOException {
        return new FileEntity(request);
    }

    public void setCacheDir(final String cacheDir) {
        this.cacheDir = Path.of(cacheDir);
    }

    public void setEngine(final Engine engine) {
        this.engine = engine;
    }

    public void setCacheCycle(final FileDateCacheCycle fileDateCacheCycle) {
        this.fileDateCacheCycle = fileDateCacheCycle;
    }
}