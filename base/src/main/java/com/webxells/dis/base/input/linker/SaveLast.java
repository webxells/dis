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
package com.webxells.dis.base.input.linker;

import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.MappingPoint;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.input.linker.JoinLinker;
import com.webxells.dis.base.SimpleMappingPortrayal;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Description("Linker to save last x read data")
public class SaveLast implements JoinLinker  {
    private final LinkedList<MappingConfiguration> last = new LinkedList<>();

    @Description("amount of saving data")
    @Default("1")
    private int amount = 1;

    @Description("prefix name for linker - \"$amount\" will be replaced by provided parameter")
    @Default("\"last-$amount:\" => e.g. for input \"example\": \"last-1:example\"")
    private String name = "last-$amount:";

    @Override
    public int getData(final MappingConfiguration from) throws InputOutputError {
        add(from);
        final int result = copyToCurrent(from);
        if (amount < last.size()) {
            last.removeLast();
        }
        return result;
    }

    private int copyToCurrent(final MappingConfiguration from) {
        final AtomicInteger count = new AtomicInteger();
        for (int i = 1, m = last.size(); i < m; i++) {
            final MappingConfiguration copy = last.get(i);
            for (MappingPart part : copy.parts()) {
                from.getByPortrayal(getPortrayal(i, part))
                                .ifPresent(a -> {
                                    a.getSubData().clear();
                                    a.copyValues(part);
                                    count.incrementAndGet();
                                });
            }
        }
        return count.get();
    }

    private MappingPortrayal getPortrayal(final int i, final MappingPart part) {
        return new SimpleMappingPortrayal(getReference(part.getInput(), i), getPath(part.getInput()));
    }

    private String getReference(final MappingPoint input, final int i) {
        return getPrefix(i).concat(Optional.ofNullable(input)
                .map(MappingPoint::getReference)
                .orElse(""));
    }

    private String getPath(final MappingPoint input) {
        return Optional.ofNullable(input)
                .map(MappingPoint::getPath)
                .orElse(null);
    }

    private void add(final MappingConfiguration from) {
        last.addFirst(from.copy());
    }

    @Override
    public String getInputName() {
        return getPrefix(amount);
    }

    private String getPrefix(final int amount) {
        return name.replace("$amount", String.valueOf(amount));
    }

    @Override
    public void start() throws DisException { }

    @Override
    public void end() throws DisException {
        last.clear();
    }

    public void setAmount(final int amount) {
        this.amount = amount;
    }

    public void setName(final String name) {
        this.name = name;
    }
}