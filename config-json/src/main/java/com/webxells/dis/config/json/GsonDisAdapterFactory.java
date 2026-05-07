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
package com.webxells.dis.config.json;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.webxells.dis.api.config.ConfigurableByType;
import com.webxells.dis.api.config.DisApi;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.boot.KnownTypeMapping;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GsonDisAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> type) {
        final Class<? super T> rawType = type.getRawType();
        if ((List.class.equals(rawType) || Set.class.equals(rawType)) && type.getType() instanceof ParameterizedType) {
            return handleCollectionType(gson, (ParameterizedType) type.getType(), Set.class.equals(rawType));
        }
        if (Map.class.equals(rawType) && type.getType() instanceof ParameterizedType) {
            return handleMapType(gson, (ParameterizedType) type.getType());
        }
        if (MappingConfiguration.class.isAssignableFrom(rawType)) {
            return new GsonDisMappingConfigAdapter<>(gson, this);
        }
        if (rawType.isInterface() && ConfigurableByType.class.isAssignableFrom(rawType)) {
            return new GsonDisDynamicTypeAdapter<>(gson, this, KnownTypeMapping.getKnown(rawType));
        }
        if (DisApi.class.isAssignableFrom(rawType)) {
            return new GsonUseFieldForSetterAdapter<>(gson, KnownTypeMapping.mapClass(rawType));
        }
        return null;
    }

    private <T> TypeAdapter<T> handleMapType(final Gson gson, final ParameterizedType type) {
        Type[] actualTypeArguments = type.getActualTypeArguments();
        if (null != actualTypeArguments && 2 == actualTypeArguments.length
                && String.class.equals(actualTypeArguments[0])) {
            return new GsonDisMapTypeAdapter<>(gson, TypeToken.get(actualTypeArguments[1]));
        }
        return null;
    }

    private <T> TypeAdapter<T> handleCollectionType(final Gson gson, final ParameterizedType type, final boolean isSet) {
        if (type.getActualTypeArguments()[0] instanceof Class<?> implementation && ConfigurableByType.class.isAssignableFrom(implementation)) {
            return new GsonDisCollectionTypeAdapter<>(create(gson,TypeToken.get((Class<ConfigurableByType>) implementation)), isSet);
        }
        return null;
    }

}