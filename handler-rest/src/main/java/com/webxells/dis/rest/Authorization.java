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
package com.webxells.dis.rest;

import com.webxells.dis.api.config.DisConfigApi;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;

@Description("Contains authorization data")
public class Authorization implements DisConfigApi {
    public enum Type {
        BASIC
    }

    private String user;
    private String password;
    @Default("BASIC")
    private Type type = Type.BASIC;

    public Authorization() {}

    public Authorization(String user, String password) {
        this.user = user;
        this.password = password;
    }


    public String getUser() {
        return user;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public Type getType() {
        return type;
    }

    public void setType(final Type type) {
        this.type = type;
    }
}