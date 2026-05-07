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
package com.webxells.dis.base.trigger;

import com.webxells.dis.api.trigger.Trigger;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class AndCondition extends Condition<AndConditionConfig> {
    private final Queue<Map.Entry<Trigger<?>, LocalDateTime>> triggeredHistory = new LinkedList<>();

    public AndCondition(final AndConditionConfig configuration) {
        super(configuration);
    }

    @Override
    protected synchronized void hullabaloo(final Trigger<?> trigger) {
        triggeredHistory.add(Map.entry(trigger, LocalDateTime.now()));
        triggeredHistory.removeIf(a -> a.getValue().isBefore(calculateMinPertainDate()));
        final boolean result = children.stream()
                .allMatch(a -> triggeredHistory.stream().anyMatch(b -> b.getKey() == a));
        if (result) {
            triggeredHistory.clear();
            action.run();
        }
    }

    private LocalDateTime calculateMinPertainDate() {
        return LocalDateTime.now().minus(configuration.getResetInterval(), configuration.getResetUnit());
    }
}
