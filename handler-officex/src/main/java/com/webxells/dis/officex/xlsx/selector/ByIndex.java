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
package com.webxells.dis.officex.xlsx.selector;

import com.webxells.dis.api.config.description.Description;

@Description("sheet index starting by 0")
public class ByIndex implements SheetSelector {

    @Description("defaults to start")
    private int from = -1;
    @Description("defaults to end")
    private int to = -1;

    @Override
    public boolean approve(final String name, final int index) {
        return from <= index && (to >= index || to == -1);
    }

    public void setFrom(final int from) {
        this.from = from;
    }

    public void setTo(final int to) {
        this.to = to;
    }
}