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
package com.webxells.dis.api.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public interface Response {
    enum ResponseState {
        UNHANDLED, HEADERS_SENT, SENT
    }

    void setStatus(int status);

    void setHeaders(Map<String, String> headers);

    void addHeader(String key, String value);

    ResponseState state();

    void sendHeaders() throws IOException;

    void send() throws IOException;

    void send(int status) throws IOException;

    OutputStream getBody();

    Request getRequest();
}