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

import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.base.resource.StringResource;
import com.webxells.dis.base.trigger.group.GroupingTrigger;
import com.webxells.dis.base.trigger.group.GroupingTriggerConfiguration;
import com.webxells.dis.base.trigger.group.GroupingTriggerConfiguration.ErrorStrategy;
import com.webxells.dis.base.trigger.group.GroupingTriggerConfiguration.UpdateEvent;
import com.webxells.dis.plain.MultiMapping;
import com.webxells.dis.plain.output.EchoConfig;
import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.example.trigger.TestTriggerConfig;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GroupingTriggerTest extends SimpleTestCase {

    @Test
    void testUpdateStatus() throws InterruptedException, InputOutputError {
        final RuntimeException error = new RuntimeException("something funny");
        ByteArrayOutputStream errorTrace = new ByteArrayOutputStream();
        error.printStackTrace(new PrintStream(errorTrace));
        final StringResource updateOutput = new StringResource();
        EchoConfig echoConfig = new EchoConfig();
        echoConfig.setTemplate("$name $start $end $status *children;;");
        echoConfig.setMultiMapping(Map.of("children", new MultiMapping() {{
            setTemplate("$name $status $start $end $error $error-trace");
            setDelimiter(";");
        }}));
        echoConfig.setSender(updateOutput);
        echoConfig.setName("test");
        AtomicBoolean starter = new AtomicBoolean();
        String group = random();
        AtomicInteger countChild1 = new AtomicInteger();
        AtomicInteger countChild2 = new AtomicInteger();
        AtomicInteger countChild3 = new AtomicInteger();
        AtomicInteger countChild4 = new AtomicInteger();
        GroupingTriggerConfiguration child2Config = new GroupingTriggerConfiguration();
        child2Config.setName("child2");
        child2Config.setGroup(group);
        GroupingTrigger child2 = new GroupingTrigger(child2Config);
        GroupingTriggerConfiguration child1Config = new GroupingTriggerConfiguration();
        child1Config.setName("child1");
        child1Config.setGroup(group);
        child1Config.setMaxParallel(2);
        child1Config.setSleepInterval(51);
        child1Config.setStatusUpdateDateFormat("'date'");
        child1Config.setStatusUpdateOutput(echoConfig);
        child1Config.setErrorStrategy(ErrorStrategy.CONTINUE);
        child1Config.setEventsToUpdate(List.of(UpdateEvent.END, UpdateEvent.START, UpdateEvent.ERROR));
        child1Config.setStart(new TestTriggerConfig(starter));
        GroupingTrigger child1 = new GroupingTrigger(child1Config);
        GroupingTriggerConfiguration child3Config = new GroupingTriggerConfiguration();
        child3Config.setName("child3");
        child3Config.setGroup(group);
        GroupingTrigger child3 = new GroupingTrigger(child3Config);
        GroupingTriggerConfiguration child4Config = new GroupingTriggerConfiguration();
        child4Config.setName("child4");
        child4Config.setGroup(group);
        GroupingTrigger child4 = new GroupingTrigger(child4Config);

        allFalse(child1.isRunning(), child2.isRunning(), child3.isRunning(),
                child4.isRunning());
        assertEquals(0, countChild1.get());
        assertEquals(0, countChild2.get());
        assertEquals(0, countChild3.get());
        assertEquals(0, countChild4.get());

        child1.awaitAction(countChild1::incrementAndGet);
        child2.awaitAction(countChild2::incrementAndGet);
        child3.awaitAction(countChild3::incrementAndGet);
        child4.awaitAction(() -> {
            throw error;
        });

        allTrue(child1.isRunning(), child2.isRunning(), child3.isRunning(),
                child4.isRunning());

        starter.set(true);

        Thread.sleep(800);
        assertEquals(1, countChild1.get());
        assertEquals(1, countChild2.get());
        assertEquals(1, countChild3.get());
        assertEquals(0, countChild4.get());
        assertEquals(String.format("%s date $end STARTED " +
                "child1 INITIALIZED $start $end $error $error-trace;" +
                "child2 INITIALIZED $start $end $error $error-trace;" +
                "child3 INITIALIZED $start $end $error $error-trace;" +
                "child4 INITIALIZED $start $end $error $error-trace;;" +
                "%s date date FINISHED (ERROR) " +
                "child1 FINISHED date date $error $error-trace;" +
                "child2 FINISHED date date $error $error-trace;" +
                "child3 FINISHED date date $error $error-trace;" +
                "child4 ERROR date date RuntimeException: something funny %s;;"
                , group, group, errorTrace), updateOutput.send().toString());
    }

    @Test
    void test() throws InterruptedException {
        AtomicBoolean starter = new AtomicBoolean();
        String group = random();
        AtomicInteger countMaster = new AtomicInteger();
        AtomicInteger countSlave1 = new AtomicInteger();
        AtomicInteger countSlave2 = new AtomicInteger();
        AtomicInteger countSlave3 = new AtomicInteger();
        AtomicInteger countSlave4 = new AtomicInteger();
        GroupingTriggerConfiguration child1Config = new GroupingTriggerConfiguration();
        child1Config.setName("test1");
        child1Config.setGroup(group);
        GroupingTrigger child1 = new GroupingTrigger(child1Config);
        GroupingTriggerConfiguration masterConfig = new GroupingTriggerConfiguration();
        masterConfig.setName("master");
        masterConfig.setGroup(group);
        masterConfig.setMaxParallel(2);
        masterConfig.setSleepInterval(51);
        masterConfig.setStart(new TestTriggerConfig(starter));
        GroupingTrigger master = new GroupingTrigger(masterConfig);
        GroupingTriggerConfiguration child2Config = new GroupingTriggerConfiguration();
        child2Config.setName("test2");
        child2Config.setGroup(group);
        GroupingTrigger child2 = new GroupingTrigger(child2Config);
        GroupingTriggerConfiguration child3Config = new GroupingTriggerConfiguration();
        child3Config.setName("test3");
        child3Config.setGroup(group);
        GroupingTrigger child3 = new GroupingTrigger(child3Config);
        GroupingTriggerConfiguration child4Config = new GroupingTriggerConfiguration();
        child4Config.setName("test4");
        child4Config.setGroup(group);
        GroupingTrigger child4 = new GroupingTrigger(child4Config);

        allFalse(master.isRunning(), child1.isRunning(), child2.isRunning(),
                child3.isRunning(), child4.isRunning());
        assertEquals(0, countMaster.get());
        assertEquals(0, countSlave1.get());
        assertEquals(0, countSlave2.get());
        assertEquals(0, countSlave3.get());

        master.awaitAction(countMaster::incrementAndGet);
        child1.awaitAction(countSlave1::incrementAndGet);
        child2.awaitAction(countSlave2::incrementAndGet);
        child3.awaitAction(() -> {
            try {
                Thread.sleep(900);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            countSlave3.incrementAndGet();
        });
        child4.awaitAction(countSlave4::incrementAndGet);

        allTrue(master.isRunning(), child1.isRunning(), child2.isRunning(),
                child3.isRunning(), child4.isRunning());

        starter.set(true);

        Thread.sleep(800);
        assertEquals(1, countMaster.get());
        assertEquals(1, countSlave1.get());
        assertEquals(1, countSlave2.get());
        assertEquals(0, countSlave3.get());
        assertEquals(1, countSlave4.get());

        Thread.sleep(400);
        assertEquals(1, countSlave3.get());
    }

}