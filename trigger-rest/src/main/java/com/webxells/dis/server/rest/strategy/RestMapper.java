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
package com.webxells.dis.server.rest.strategy;

import com.webxells.dis.api.rest.Request.Method;
import com.webxells.dis.api.rest.filter.RequestFilter;
import java.util.List;
import java.util.Objects;

public class RestMapper {
    private String path;
    private Method method;
    private List<RequestFilter> requestFilters;

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(final Method method) {
        this.method = method;
    }

    public List<RequestFilter> getRequestFilters() {
        return requestFilters;
    }

    public void setRequestFilters(final List<RequestFilter> requestFilters) {
        this.requestFilters = requestFilters;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof RestMapper)) return false;
        final RestMapper that = (RestMapper) o;
        return Objects.equals(path, that.path) && method == that.method;
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, method);
    }
}