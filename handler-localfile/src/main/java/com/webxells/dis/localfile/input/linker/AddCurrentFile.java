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
package com.webxells.dis.localfile.input.linker;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.input.linker.JoinLinker;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.localfile.FileNameStore;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Description("Provides path information of current file loaded by MultipleLocalFiles - " +
             "Recognizes PATH (absolute file path), FILE (filename with extension), TYPE (mime type) " +
             "and FILE_WITHOUT_TYPE (filename without extension)")
public class AddCurrentFile implements JoinLinker {
    public enum ContentType {
        PATH, FILE, TYPE, FILE_WITHOUT_TYPE
    }

    @Required
    @Description("Reference of the handler")
    private String name;

    @Override
    public void validate() throws InvalidApi {
        if (null == name) {
            throw new InvalidApi("required fields missing");
        }
    }

    @Override
    public int getData(final MappingConfiguration from) {
        final Set<String> allTypes = Arrays.stream(ContentType.values())
                .map(Enum::name)
                .collect(Collectors.toSet());
        final AtomicInteger count = new AtomicInteger();

        from.partsBySource(name).stream()
                .filter(a -> null != a.getInput() && null != a.getInput().getPath())
                .filter(a -> allTypes.contains(a.getInput().getPath()))
                .forEach(a -> {
                    a.getDataset().collect(
                            new SimpleDatasetPiece(
                                    createContentByType(ContentType.valueOf(a.getInput().getPath()))));
                    count.incrementAndGet();
                });
        return count.get();
    }

    private String createContentByType(final ContentType type) {
        return switch (type) {
            case PATH -> FileNameStore.getPath(name);
            case FILE -> FileNameStore.getFileName(name);
            case TYPE -> FileNameStore.getFileType(name);
            case FILE_WITHOUT_TYPE -> FileNameStore.getFileNameWithoutType(name);
        };
    }

    @Override
    public String getInputName() {
        return name;
    }
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public void start() { }

    @Override
    public void end() { }
}