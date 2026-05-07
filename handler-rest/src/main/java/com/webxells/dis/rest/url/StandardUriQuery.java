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
package com.webxells.dis.rest.url;

import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.error.InvalidApi;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;
import java.util.stream.Collectors;

public class StandardUriQuery implements ParameterParser {
    public static final String DEFAULT_PARAMETER_ENCODING = StandardCharsets.UTF_8.name();

    private Map<String, String> parameters;
    @Default("?")
    private String startString = "?";
    @Default("&")
    private String parameterDelimiter = "&";
    @Default("=")
    private String parameterAssignment = "=";
    @Default("UTF_8")
    private String parameterEncoding = DEFAULT_PARAMETER_ENCODING;

    public static String encode(final String value, final String parameterEncoding) {
        try {
            return URLEncoder.encode(value, parameterEncoding).replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Invalid encoding", e);
        }
    }

    public static String decode(final String value, final String parameterEncoding) {
        try {
            return URLDecoder.decode(value.replace("%20", "+"), parameterEncoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Invalid encoding", e);
        }
    }

    @Override
    public void validate() throws InvalidApi {
        try {
            Charset.forName(parameterEncoding);
        } catch (final UnsupportedCharsetException e) {
            throw new InvalidApi("Unsupported charset defined: ".concat(parameterEncoding), e);
        }
    }

    @Override
    public String parse() {
        return startString.concat(parameters.entrySet().stream()
                .map(a -> String.format("%s%s%s", encode(a.getKey(), parameterEncoding),
                        parameterAssignment, encode(a.getValue(), parameterEncoding)))
                .collect(Collectors.joining(parameterDelimiter)));
    }

    @Override
    public Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    public String getType() {
        return StandardUriQuery.class.getName();
    }

    public void setParameters(final Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public void setStartString(final String startString) {
        this.startString = startString;
    }

    public void setParameterDelimiter(final String parameterDelimiter) {
        this.parameterDelimiter = parameterDelimiter;
    }

    public void setParameterAssignment(final String parameterAssignment) {
        this.parameterAssignment = parameterAssignment;
    }

    public void setParameterEncoding(final String parameterEncoding) {
        this.parameterEncoding = parameterEncoding;
    }
}