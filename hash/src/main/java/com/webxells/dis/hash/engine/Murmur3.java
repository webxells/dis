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
/*
 * based on https://github.com/apache/hive/blob/master/storage-api/src/java/org/apache/hive/common/util/Murmur3.java
 *      (2019-09-05T13:18:23)
 * striped and enhanced for simplicity
 */

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.webxells.dis.api.hash.Engine;
import com.webxells.dis.hash.intern.FixedBufferedInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Murmur3 is successor to Murmur2 fast non-crytographic hash algorithms.
 * Murmur3 128 bit variants.
 * 128-bit Java port of <a href="https://code.google.com/p/smhasher/source/browse/trunk/MurmurHash3.cpp#255">...</a>
 * This is a public domain code with no copyrights.
 * From homepage of MurmurHash (<a href="https://code.google.com/p/smhasher/">...</a>),
 * "All MurmurHash versions are public domain software, and the author disclaims all copyright
 * to their code."
 */
public class Murmur3 implements Engine {
    private static final int BUFFER_READ_SIZE = 4096;
    private static final long C1 = 0x87c37b91114253d5L;
    private static final long C2 = 0x4cf5ad432745937fL;
    private static final int R1 = 31;
    private static final int R2 = 27;
    private static final int R3 = 33;
    private static final int M = 5;
    private static final int N1 = 0x52dce729;
    private static final int N2 = 0x38495ab5;

    private int seed = 10666;

    @Override
    public String convert(final String data) {
        return convert(data.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String convert(final InputStream stream) throws IOException {
        final FixedBufferedInputStream fixedBis = new FixedBufferedInputStream(stream, BUFFER_READ_SIZE);
        String result = "";
        byte[] readBuffer;
        while (null != (readBuffer = fixedBis.read())) {
            final byte[] resultBytes = result.getBytes(StandardCharsets.UTF_8);
            final int resultLength = resultBytes.length;
            final int readLength = readBuffer.length;
            final byte[] buffer = new byte[readLength + resultLength];
            System.arraycopy(resultBytes, 0, buffer, 0, resultLength);
            System.arraycopy(readBuffer, 0, buffer, resultLength, readLength);
            result = convert(buffer);
        }
        fixedBis.close();
        return result;
    }

    public void setSeed(final int seed) {
        this.seed = seed;
    }

    private String convert(final byte[] data) {
        return Arrays.stream(hash128(data, seed))
                .mapToObj(Long::toHexString)
                .map(a -> a.replace('-', 'g'))
                .collect(Collectors.joining());
    }

    /**
     * Murmur3 128-bit variant.
     *
     * @param data   - input byte array
     * @param seed   - seed. (default is 0)
     * @return - hashcode (2 longs)
     */
    private static long[] hash128(byte[] data, int seed) {
        final int length = data.length;
        final int nblocks = length >> 4;
        long h1 = seed;
        long h2 = seed;

        // body
        for (int i = 0; i < nblocks; i++) {
            final int i16 = i << 4;
            long k1 = ((long) data[i16] & 0xff)
                    | (((long) data[i16 + 1] & 0xff) << 8)
                    | (((long) data[i16 + 2] & 0xff) << 16)
                    | (((long) data[i16 + 3] & 0xff) << 24)
                    | (((long) data[i16 + 4] & 0xff) << 32)
                    | (((long) data[i16 + 5] & 0xff) << 40)
                    | (((long) data[i16 + 6] & 0xff) << 48)
                    | (((long) data[i16 + 7] & 0xff) << 56);

            long k2 = ((long) data[i16 + 8] & 0xff)
                    | (((long) data[i16 + 9] & 0xff) << 8)
                    | (((long) data[i16 + 10] & 0xff) << 16)
                    | (((long) data[i16 + 11] & 0xff) << 24)
                    | (((long) data[i16 + 12] & 0xff) << 32)
                    | (((long) data[i16 + 13] & 0xff) << 40)
                    | (((long) data[i16 + 14] & 0xff) << 48)
                    | (((long) data[i16 + 15] & 0xff) << 56);

            h1 = generateH(h1, h2, k1, C1, R1, C2, R2, N1);

            h2 = generateH(h2, h1, k2, C2, R3, C1, R1, N2);
        }

        // tail
        final int tailStart = nblocks << 4;
        long k1 = 0;
        long k2 = 0;
        switch (length - tailStart) {
            case 15:
                k2 ^= (long) (data[tailStart + 14] & 0xff) << 48;
            case 14:
                k2 ^= (long) (data[tailStart + 13] & 0xff) << 40;
            case 13:
                k2 ^= (long) (data[tailStart + 12] & 0xff) << 32;
            case 12:
                k2 ^= (long) (data[tailStart + 11] & 0xff) << 24;
            case 11:
                k2 ^= (long) (data[tailStart + 10] & 0xff) << 16;
            case 10:
                k2 ^= (long) (data[tailStart + 9] & 0xff) << 8;
            case 9:
                k2 ^= data[tailStart + 8] & 0xff;
                k2 *= C2;
                k2 = Long.rotateLeft(k2, R3);
                k2 *= C1;
                h2 ^= k2;

            case 8:
                k1 ^= (long) (data[tailStart + 7] & 0xff) << 56;
            case 7:
                k1 ^= (long) (data[tailStart + 6] & 0xff) << 48;
            case 6:
                k1 ^= (long) (data[tailStart + 5] & 0xff) << 40;
            case 5:
                k1 ^= (long) (data[tailStart + 4] & 0xff) << 32;
            case 4:
                k1 ^= (long) (data[tailStart + 3] & 0xff) << 24;
            case 3:
                k1 ^= (long) (data[tailStart + 2] & 0xff) << 16;
            case 2:
                k1 ^= (long) (data[tailStart + 1] & 0xff) << 8;
            case 1:
                k1 ^= data[tailStart] & 0xff;
                k1 *= C1;
                k1 = Long.rotateLeft(k1, R1);
                k1 *= C2;
                h1 ^= k1;
        }

        // finalization
        h1 ^= length;
        h2 ^= length;

        h1 += h2;
        h2 += h1;

        h1 = fmix64(h1);
        h2 = fmix64(h2);

        h1 += h2;
        h2 += h1;

        return new long[] {h1, h2};
    }

    private static long generateH(long h1, final long h2, long k1, final long c1, final int r1, final long c2, final int r2, final int n1) {
        k1 *= c1;
        k1 = Long.rotateLeft(k1, r1);
        k1 *= c2;
        h1 ^= k1;
        h1 = Long.rotateLeft(h1, r2);
        h1 += h2;
        h1 = h1 * M + n1;
        return h1;
    }

    private static long fmix64(long h) {
        h ^= (h >>> 33);
        h *= 0xff51afd7ed558ccdL;
        h ^= (h >>> 33);
        h *= 0xc4ceb9fe1a85ec53L;
        h ^= (h >>> 33);
        return h;
    }
}