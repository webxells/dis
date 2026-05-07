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
package com.webxells.dis.info.internal.config.transformer;

import com.webxells.dis.info.internal.ForEachObject;
import com.webxells.dis.info.internal.clazz.Implementation;
import com.webxells.dis.info.internal.clazz.Option;
import com.webxells.dis.info.internal.config.RootConfiguration;
import com.webxells.dis.info.internal.config.transformer.option.AliasOption;
import com.webxells.dis.info.internal.config.transformer.option.DefaultDescription;
import com.webxells.dis.info.internal.config.transformer.option.Mappable;
import com.webxells.dis.info.internal.config.transformer.option.Multiple;
import com.webxells.dis.info.internal.config.transformer.option.Name;
import com.webxells.dis.info.internal.config.transformer.option.OptionDescription;
import com.webxells.dis.info.internal.config.transformer.option.RequiredOption;
import com.webxells.dis.info.internal.config.transformer.option.Type;
import java.util.List;

public class Options extends SubData<Implementation, Option> {

    public Options(final RootConfiguration.Configuration configuration) {
        super(configuration, List.of(
                new Name(configuration),
                new OptionDescription(configuration),
                new DefaultDescription(configuration),
                new Multiple(configuration),
                new Mappable(configuration),
                new RequiredOption(configuration),
                new Type(configuration),
                new AliasOption(configuration)
        ));
    }

    @Override
    protected ForEachObject<Option> getObjects(final Implementation current) {
        return current.readOptions();
    }

    @Override
    public String path() {
        return "options";
    }

}