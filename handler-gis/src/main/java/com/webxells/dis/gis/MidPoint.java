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
package com.webxells.dis.gis;

import com.webxells.dis.gis.coordinate.Xy;

public class MidPoint {
    private Double xMax;
    private Double xMin;
    private Double yMax;
    private Double yMin;

    public void add(final Xy point) {
        final double x = point.x();
        final double y = point.y();
        if (notFilled()) {
            xMin = xMax = x;
            yMin = yMax = y;
        } else {
            if (x > xMax) {
                xMax = x;
            } else if (x < xMin) {
                xMin = x;
            }
            if (y > yMax) {
                yMax = y;
            } else if (y < yMin) {
                yMin = y;
            }
        }
    }

    public Xy toXy() {
        return new Xy((xMax + xMin) / 2, (yMax + yMin) / 2, null);
    }

    public boolean notFilled() {
        return null == xMax;
    }
}