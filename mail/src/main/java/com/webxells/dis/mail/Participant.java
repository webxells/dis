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
package com.webxells.dis.mail;

import com.webxells.dis.api.config.ConfigurableByType;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

public class Participant implements ConfigurableByType {
    @Required
    private String email;
    private String name;
    @Description("Enforce RFC822 syntax")
    @Default("false")
    private boolean forceRfc822;
    private InternetAddress address;

    @Override
    public void validate() throws InvalidApi {
        if (null == email) {
            throw new InvalidApi("email is required");
        }
    }

    public InternetAddress getAddress() throws AddressException {
        String address = email;
        if (null != name) {
            address = String.format("%s <%s>", name, address);
        }
        return new InternetAddress(address, forceRfc822);
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setForceRfc822(final boolean forceRfc822) {
        this.forceRfc822 = forceRfc822;
    }
}