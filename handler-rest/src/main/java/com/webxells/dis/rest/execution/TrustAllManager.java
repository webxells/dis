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
package com.webxells.dis.rest.execution;

import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

public class TrustAllManager implements X509TrustManager {
    private static final TrustAllManager instance = new TrustAllManager();

    public static TrustAllManager getInstance() {
        return instance;
    }

    private TrustAllManager() { }

    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType) { }

    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType) { }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[]{};
    }
}