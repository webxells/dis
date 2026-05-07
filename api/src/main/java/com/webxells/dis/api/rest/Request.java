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


import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface Request {
    enum Method {
        POST, GET, PATCH, DELETE, HEAD
    }

    String getPath();

    String getFullPath();

    Method getMethod();

    public InputStream getBody();

    Map<String, List<String>> getHeaders();

    Map<String, List<String>> getUrlParameters();

    Map<String, List<String>> getFormParameters();

    Map<String, Request> getMultiParts();

    Map<String, List<File>> getFiles();

    default void clear() { }

    Response respond();
}