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
package com.webxells.dis.logging.simple.internal;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Ring<T> {
    private final Queue<T> values = new ConcurrentLinkedQueue<>();
    private int capacity;

    public Ring() {
        this(1);
    }

    public Ring(final int capacity) {
        setCapacity(capacity);
    }

    public void setCapacity(final int capacity) {
        if (capacity < this.capacity) {
            makeRoom(this.capacity - capacity);
        }
        this.capacity = capacity;
    }

    private void makeRoom(int i) {
        while (i-- > 0) {
            values.poll();
        }
    }

    public void add(final T value) {
        if (capacity > 0) {
            values.add(value);
            makeRoom(values.size() - capacity);
        }
    }

    public List<T> get() {
        return new LinkedList<>(values);
    }

    public void clear() {
        values.clear();
    }
}