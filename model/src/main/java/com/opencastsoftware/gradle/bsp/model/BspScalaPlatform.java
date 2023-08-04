/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.model;

public enum BspScalaPlatform {
    JVM(1),
    JS(2),
    NATIVE(3);

    private final int value;

    BspScalaPlatform(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}
