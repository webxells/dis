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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.DisApi;
import com.webxells.dis.boot.KnownTypeMapping;
import com.webxells.dis.config.json.intern.SourceMapping;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.io.CharArrayReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

class GsonUseFieldForSetterAdapter<T> extends ValidatingReader<T> {
    private static final Logger LOGGER = LoggerProxyFactory.logger(JsonConfig.class);
    private final Class<? super T> clazz;
    private final Gson gson;

    public static <T> void fillExisting(final T instance, final JsonObject object, final Gson gson)
            throws InvocationTargetException, IllegalAccessException {
        final Class<?> clazz = instance.getClass();
        for (Map.Entry<String, JsonElement> current : object.entrySet()) {
            final String key = current.getKey();
            if (SourceMapping.getMappingKey().equals(key)) {
                SourceMapping.register(current.getValue(), instance);
                continue;
            }
            final Method method = guessMethod(key, clazz, toToken(current.getValue()));
            if (null != method) {
                callMethod(method, instance, object.get(key), gson);
            }
        }
    }

    private static JsonToken toToken(final JsonElement value) {
        if (value.isJsonObject()) {
            return JsonToken.BEGIN_OBJECT;
        }
        if (value.isJsonArray()) {
            return JsonToken.BEGIN_ARRAY;
        }
        if (value.isJsonNull()) {
            return JsonToken.NULL;
        }
        return JsonToken.STRING;
    }

    private static <T> void callMethod(final Method method, final T instance, final JsonElement jsonElement, final Gson gson)
            throws InvocationTargetException, IllegalAccessException {
        final Class<?> parameterType = method.getParameterTypes()[0];
        if (Integer.class == parameterType || int.class == parameterType) {
            method.invoke(instance, jsonElement.getAsJsonPrimitive().getAsInt());
        } else if (Double.class == parameterType || double.class == parameterType
                || Float.class == parameterType || float.class == parameterType) {
            method.invoke(instance, jsonElement.getAsJsonPrimitive().getAsDouble());
        } else if (Long.class == parameterType || long.class == parameterType) {
            method.invoke(instance, jsonElement.getAsJsonPrimitive().getAsLong());
        } else if (Boolean.class == parameterType || boolean.class == parameterType) {
            method.invoke(instance, jsonElement.getAsJsonPrimitive().getAsBoolean());
        } else if (String.class == parameterType) {
            method.invoke(instance, jsonElement.getAsJsonPrimitive().getAsString());
        } else if (Character.class == parameterType || char.class == parameterType) {
            method.invoke(instance, jsonElement.getAsJsonPrimitive().getAsCharacter());
        } else if (List.class == parameterType || Map.class == parameterType || Set.class == parameterType) {
            method.invoke(instance, parameterType.cast(gson.fromJson(jsonElement, method.getGenericParameterTypes()[0])));
        } else {
            method.invoke(instance, parameterType.cast(gson.fromJson(jsonElement, parameterType)));
        }
    }

    private static Method guessMethod(final String fieldName, final Class<?> clazz, final JsonToken value) {
        final String methodName = String.format("set%s%s", fieldName.substring(0,1).toUpperCase(),
                fieldName.substring(1));
        final Method method = Arrays.stream(clazz.getMethods())
                .filter(a -> a.getName().equals(methodName) && a.getParameterCount() == 1)
                .filter(a -> fitToJson(a.getParameterTypes()[0], value))
                .findFirst().orElse(null);
        if (null == method && !"type".equals(fieldName)) {
            LOGGER.info(String.format("Method %s for class %s not found! Skipping...",
                    methodName, clazz.getName()));
        }
        return method;
    }

    private static boolean fitToJson(final Class<?> parameter, final JsonToken value) {
        final boolean isCollection = List.class == parameter || Set.class == parameter || parameter.isArray();
        if (JsonToken.NULL == value) {
            return true;
        }
        if (JsonToken.BEGIN_ARRAY == value && isCollection) {
            return true;
        }
        if (isPrimitive(value) && isPrimitive(parameter)) {
            return true;
        }
        return JsonToken.BEGIN_OBJECT == value && !isCollection;
    }

    private static boolean isPrimitive(final JsonToken value) {
        return JsonToken.STRING == value || JsonToken.BOOLEAN == value || JsonToken.NUMBER == value;
    }

    private static boolean isPrimitive(final Class<?> clazz) {
        return clazz.isPrimitive() || clazz.isEnum() ||
                Integer.class == clazz || Long.class == clazz ||Float.class == clazz ||
                Double.class == clazz || Boolean.class == clazz || String.class == clazz || Character.class == clazz;
    }

    GsonUseFieldForSetterAdapter(final Gson gson, final Class<? super T> type) {
        clazz = type;
        this.gson = gson;
    }

    @Override
    T readClass(final JsonReader in) throws IOException {
        if (JsonToken.NULL.equals(in.peek())) {
            in.nextNull();
            return null;
        } else if(JsonToken.BEGIN_OBJECT.equals(in.peek())) {
            T instance;
            try {
                instance = createFullObject(in);
            } catch (final NoSuchMethodException | InstantiationException |
                           IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Error while setting members of: ".concat(clazz.getName()), e);
            }
            in.endObject();
            return instance;
        }
        throw new RuntimeException("Not an object");
    }

    private T createFullObject(final JsonReader in) throws NoSuchMethodException, IOException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
        final T instance = (T) clazz.getConstructor().newInstance();
        final List<String> methods = new ArrayList<>();

        callDeclaredMethods(instance, in, methods);
        callUndeclaredMethods(instance, methods);

        return instance;
    }

    private void callUndeclaredMethods(final T instance, final List<String> methods) throws IOException, InvocationTargetException, IllegalAccessException {
        for (final Method method : instance.getClass().getDeclaredMethods()) {
            final Optional<String> knownMethod = getIfUndeclaredKnownMethod(method, methods);
            if (knownMethod.isPresent()) {
                callMethod(method, instance, getDefaultTypeReader(knownMethod.get()));
            }
        }
    }

    private void callDeclaredMethods(final T instance, final JsonReader in, final List<String> methods) throws IOException, InvocationTargetException, IllegalAccessException {
        in.beginObject();
        while (in.hasNext()) {
            final Method method = guessMethod(in, instance);
            if (null == method) {
                continue;
            }
            methods.add(method.getName());
            callMethod(method, instance, in);
        }
    }

    private JsonReader getDefaultTypeReader(final String type) {
        return new JsonReader(new CharArrayReader(String.format("{\"type\":\"%s\"}", type).toCharArray()));
    }

    private Optional<String> getIfUndeclaredKnownMethod(final Method method, final List<String> methods) {
        final int modifiers = method.getModifiers();
        if (1 == method.getParameterCount() && !methods.contains(method.getName()) && !Modifier.isStatic(modifiers) &&
                !Modifier.isAbstract(modifiers) && Modifier.isPublic(modifiers) &&
                DisApi.class.isAssignableFrom(method.getParameterTypes()[0]) && method.getName().startsWith("set")) {
            return Optional.ofNullable(KnownTypeMapping.getForced(method.getParameterTypes()[0]));
        }
        return Optional.empty();
    }

    private Method guessMethod(final JsonReader in, final T instance) throws IOException {
        if (JsonToken.NULL != in.peek()) {
            final String fieldName = in.nextName();
            if (SourceMapping.getMappingKey().equals(fieldName)) {
                SourceMapping.register(in, instance);
                return null;
            }
            final Method result = guessMethod(fieldName, clazz, in.peek());
            if (null != result) {
                return result;
            }
        }
        in.skipValue();
        return null;
    }

    private void callMethod(final Method method, final T instance, final JsonReader in) throws IOException, InvocationTargetException, IllegalAccessException {
        Class<?> parameterType = method.getParameterTypes()[0];
        if (Integer.class == parameterType || int.class == parameterType) {
            method.invoke(instance, in.nextInt());
        } else if (Double.class == parameterType || double.class == parameterType
                || Float.class == parameterType || float.class == parameterType) {
            method.invoke(instance, in.nextDouble());
        } else if (Long.class == parameterType || long.class == parameterType) {
            method.invoke(instance, in.nextLong());
        } else if (Boolean.class == parameterType || boolean.class == parameterType) {
            method.invoke(instance, in.nextBoolean());
        } else if (String.class == parameterType) {
            method.invoke(instance, in.nextString());
        } else if (Character.class == parameterType || char.class == parameterType) {
            method.invoke(instance, in.nextString().charAt(0));
        } else if (List.class == parameterType || Map.class == parameterType || Set.class == parameterType) {
            method.invoke(instance, parameterType.cast(gson.fromJson(in, method.getGenericParameterTypes()[0])));
        } else {
            method.invoke(instance, parameterType.cast(gson.fromJson(in, parameterType)));
        }
    }
}