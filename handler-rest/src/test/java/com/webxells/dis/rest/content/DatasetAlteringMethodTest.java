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
package com.webxells.dis.rest.content;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.rest.RestConfig;
import com.webxells.dis.rest.execution.HttpMethod;
import com.webxells.dis.rest.execution.Request;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import static com.webxells.dis.test.cases.ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM;

class DatasetAlteringMethodTest extends SimpleTestCase {

    @Test
    void testInvalid() throws InvalidApi {
        DatasetAlteringMethod fixture = new DatasetAlteringMethod();
        Assertions.assertThrows(InvalidApi.class, fixture::validate);
        fixture.setRules(List.of());
        fixture.validate();
        fixture.setRules(List.of(new DatasetAlteringMethod.Rule()));
        Assertions.assertThrows(InvalidApi.class, fixture::validate);
    }

    @Test
    void testEMPTY() {
        test(newConfiguration()
                .addPart(ConfigurationBuilder.newPart(RANDOM))
                .build(), HttpMethod.PUT);
    }

    @Test
    void testISSET() {
        test(newConfiguration()
                .addPart(ConfigurationBuilder.newPart(RANDOM)
                        .withContent())
                .build(), HttpMethod.POST);
    }

    void test(MappingConfiguration configuration, HttpMethod result) {
        Request.Builder builder = Mockito.mock(Request.Builder.class);
        DatasetAlteringMethod fixture = new DatasetAlteringMethod();
        fixture.setContent(configuration);
        fixture.setRules(List.of(new DatasetAlteringMethod.Rule() {{
            checkType = Type.EMPTY;
            resultMethod = HttpMethod.PUT;
            mappingPortrayal = SimpleMappingPortrayal.destination(configuration.parts().get(0));
        }}, new DatasetAlteringMethod.Rule() {{
            checkType = Type.ISSET;
            resultMethod = HttpMethod.POST;
            mappingPortrayal = SimpleMappingPortrayal.destination(configuration.parts().get(0));
        }}));
        fixture.parseInputRequest(HttpMethod.GET, builder, new RestConfig());
        Mockito.verify(builder).method(ArgumentMatchers.eq(result));
        Mockito.verifyNoMoreInteractions(builder);
    }

}