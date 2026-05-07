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
package com.webxells.dis.boot;


import com.webxells.dis.api.config.ConfigurableByType;
import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.OutputConfig;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.resource.Resource;
import java.io.InputStream;
import java.io.OutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

interface ConfigurableByTypeInherited extends ConfigurableByType { }

class TestClass {
    public TestClass() {

    }

    public TestClass(ConfigurableByTypeInherited a) {
        ServiceManagerTest.rightConstructorChosen = true;
    }
}

class TestResource implements Resource {
    private TestResource receiver;
    private TestResource sender;

    public TestResource getReceiver() {
        return receiver;
    }

    public void setReceiver(final TestResource receiver) {
        this.receiver = receiver;
    }

    @Override
    public OutputStream send() throws InputOutputError {
        return null;
    }

    @Override
    public InputStream receive() throws InputOutputError {
        return null;
    }

    public TestResource getSender() {
        return sender;
    }

    public void setSender(final TestResource sender) {
        this.sender = sender;
    }
}
class TestInputConfigWithoutResource implements InputConfig {
    Resource receiver;

    @Override
    public String getType() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    public void setReceiver(Resource resource) {
        receiver = resource;
    }
}

class TestConfigWithGetResource implements InputConfig {

    public Resource getReceiver() {
        return Mockito.mock(Resource.class);
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

}

class TestInputConfigWithResource implements InputConfig {
    Resource receiver;

    public Resource receiver() {
        return Mockito.mock(Resource.class);
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    public void setReceiver(Resource resource) {
        receiver = resource;
    }
}
class TestOutputConfigWithoutResource implements OutputConfig {
    Resource sender;

    @Override
    public String getType() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    public void setSender(Resource sender) {
        this.sender = sender;
    }
}

class TestOutputConfigWithGetResource implements OutputConfig {
    public Resource getSender() {
        return Mockito.mock(Resource.class);
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }
}

class TestOutputConfigWithResource implements OutputConfig {
    Resource sender;

    public Resource sender() {
        return Mockito.mock(Resource.class);
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    public void setSender(Resource sender) {
        this.sender = sender;
    }
}

class ServiceManagerTest {
    static boolean rightConstructorChosen;

    @Test
    void testOverwriteResourceOfConfig() {
        Resource resource = Mockito.mock(Resource.class);

        TestInputConfigWithoutResource testInputConfigWithoutResource = new TestInputConfigWithoutResource();
        ServiceManager.overwriteResourceOfConfig(testInputConfigWithoutResource, resource);
        assertSame(resource, testInputConfigWithoutResource.receiver);

        TestInputConfigWithResource testInputConfigWithResource = new TestInputConfigWithResource();
        ServiceManager.overwriteResourceOfConfig(testInputConfigWithResource, resource);
        assertSame(resource, testInputConfigWithResource.receiver);

        ServiceManager.overwriteResourceOfConfig(new TestConfigWithGetResource(), resource);

        TestOutputConfigWithoutResource testOutputConfigWithoutResource = new TestOutputConfigWithoutResource();
        ServiceManager.overwriteResourceOfConfig(testOutputConfigWithoutResource, resource);
        assertSame(resource, testOutputConfigWithoutResource.sender);

        TestOutputConfigWithResource testOutputConfigWithResource = new TestOutputConfigWithResource();
        ServiceManager.overwriteResourceOfConfig(testOutputConfigWithResource, resource);
        assertSame(resource, testOutputConfigWithResource.sender);

        ServiceManager.overwriteResourceOfConfig(new TestOutputConfigWithGetResource(), resource);
    }

    @Test
    void testExtractResourceOfConfig() {
        assertNull(ServiceManager.extractResourceOfConfig(new TestInputConfigWithoutResource()));
        assertNotNull(ServiceManager.extractResourceOfConfig(new TestInputConfigWithResource()));
        assertNotNull(ServiceManager.extractResourceOfConfig(new TestConfigWithGetResource()));
        assertNull(ServiceManager.extractResourceOfConfig(new TestOutputConfigWithoutResource()));
        assertNotNull(ServiceManager.extractResourceOfConfig(new TestOutputConfigWithResource()));
        assertNotNull(ServiceManager.extractResourceOfConfig(new TestOutputConfigWithGetResource()));
    }
    @Test
    void testExtractResourceOfDisApi() {
        TestResource some = new TestResource();
        TestResource someChild = new TestResource();
        TestResource someGrandChild = new TestResource();
        some.setReceiver(someChild);
        some.setSender(someChild);
        someChild.setReceiver(someGrandChild);
        someChild.setSender(someGrandChild);

        assertSame(someChild, ServiceManager.extractReceiverOfConfig(some));
        assertSame(someChild, ServiceManager.extractSenderOfConfig(some));
        assertSame(someGrandChild, ServiceManager.extractReceiverOfConfig(someChild));
        assertSame(someGrandChild, ServiceManager.extractSenderOfConfig(someChild));
        assertNull(ServiceManager.extractReceiverOfConfig(someGrandChild));
        assertNull(ServiceManager.extractSenderOfConfig(someGrandChild));
    }

    @Test
    void test() {
        rightConstructorChosen = false;
        ConfigurableByTypeInherited configurableByType = Mockito.mock(ConfigurableByTypeInherited.class);
        Mockito.when(configurableByType.getType()).thenReturn(TestClass.class.getName());

        TestClass s = ServiceManager.loadByConfig(configurableByType);
        assertNotNull(s);
        assertTrue(rightConstructorChosen);
    }

    @Test
    void testWithInvalidClassShouldFail() {
        ConfigurableByType configurableByType = Mockito.mock(ConfigurableByType.class);
        Mockito.when(configurableByType.getType()).thenReturn("nothing.here");

        Assertions.assertThrows(RuntimeException.class, () -> ServiceManager.loadByConfig(configurableByType));
    }

    @Test
    void testWithValidClassButInvalidClassConstructorShouldFail() {
        ConfigurableByType configurableByType = Mockito.mock(ConfigurableByType.class);
        Mockito.when(configurableByType.getType()).thenReturn("java.lang.String");

        Assertions.assertThrows(RuntimeException.class, () -> ServiceManager.loadByConfig(configurableByType));
    }
}