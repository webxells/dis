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
package com.webxells.dis.hash.engine;

import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.hash.Engine;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class JavaIntern implements Engine {
    public String hashType;

    @Override
    public void validate() throws InvalidApi {
        try {
            newDigest();
        } catch (final NoSuchAlgorithmException e) {
            throw new InvalidApi("Could not find hashType: " + hashType, e);
        }
    }

    @Override
    public String convert(final String data) {
        try {
            final MessageDigest digest = newDigest();
            digest.update(data.getBytes());
            return toHex(digest);
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String convert(final InputStream data) throws IOException {
        try (final DigestInputStream digestInputStream = new DigestInputStream(data, newDigest())) {
            digestInputStream.readAllBytes();
            return toHex(digestInputStream.getMessageDigest());
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String toHex(final MessageDigest digest) {
        final StringBuilder result = new StringBuilder();
        for (byte b : digest.digest()) {
            result.append(String.format("%02x", b & 0xff));
        }
        return result.toString();
    }

    private MessageDigest newDigest() throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(hashType);
    }

    public void setHashType(final String hashType) {
        this.hashType = hashType;
    }
}