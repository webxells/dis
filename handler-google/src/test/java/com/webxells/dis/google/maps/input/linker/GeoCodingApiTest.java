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
package com.webxells.dis.google.maps.input.linker;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApiRequest;
import com.google.maps.errors.ApiException;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.Geometry;
import com.google.maps.model.LatLng;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.gis.PointType;
import com.webxells.dis.google.GoogleServiceConfig;
import com.webxells.dis.google.maps.internal.GeoCodingApiProxy;
import com.webxells.dis.test.cases.FileTestCase;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class GeoCodingApiTest extends FileTestCase {

    @Test
    void test() throws InvalidApi, IOException, InterruptedException, ApiException {
        new GeoCodingApi(); //lol
        GeoCodingApiProxy geoCodingApiProxy = mock(GeoCodingApiProxy.class);
        GeocodingApiRequest requestMock = mock(GeocodingApiRequest.class, withSettings().stubOnly());
        AddressComponent[] addressComponents = new AddressComponent[] {
                createComponent(AddressComponentType.STREET_NUMBER),
                createComponent(AddressComponentType.POSTAL_CODE),
                createComponent(AddressComponentType.LOCALITY),
                createComponent(AddressComponentType.WARD),
                createComponent(AddressComponentType.STREET_ADDRESS),
        };
        Geometry geometry = new Geometry();
        geometry.location = new LatLng(random(0), random(0));
        GeocodingResult result0 = new GeocodingResult();
        result0.addressComponents = addressComponents;
        result0.geometry = geometry;
        GeocodingResult result1 = new GeocodingResult();
        GeocodingResult[] callResult = new GeocodingResult[] {result0, result1};
        when(requestMock.await()).thenReturn(callResult);
        when(geoCodingApiProxy.geocode(any(), anyString())).thenReturn(requestMock);
        final SimpleMappingConfiguration config = new SimpleMappingConfiguration();
        config.setParts(List.of(
                createPart(config, AddressComponentType.LOCALITY.toString(), false),
                createPart(config, AddressComponentType.STREET_ADDRESS.toString(), false),
                createPart(config, AddressComponentType.STREET_NUMBER.toString(), false),
                createPart(config, AddressComponentType.POSTAL_CODE.toString(), false),
                createPart(config, AddressComponentType.WARD.toString(), false),
                createPart(config, "coordinates", false),
                createPart(config, "streetSource", true),
                createPart(config, "postalCodeSource", true),
                createPart(config, "citySource", true),
                createPart(config, "houseNumberSource", true),
                createPart(config, "something-other", true)
        ));

        GeoCodingApi fixture = new GeoCodingApi(geoCodingApiProxy);
        assertThrows(InvalidApi.class, fixture::validate);
        fixture.setTemplate("$streetSource $houseNumberSource, $postalCodeSource $citySource Germany");
        GoogleServiceConfig googleServiceConfig = new GoogleServiceConfig();
        googleServiceConfig.setApiKey(random("api"));
        googleServiceConfig.setConnectTimeout(random(0));
        fixture.setGoogle(googleServiceConfig);
        fixture.setMaxUsesPerMonth(4);
        fixture.setParseType(PointType.WITH_COMMA);
        assertThrows(InvalidApi.class, fixture::validate);
        fixture.setUsageSavePath(testDir.getAbsolutePath());
        fixture.validate();

        fixture.start();
        IntStream.range(0, 4)
                .parallel()
                .forEach(a -> {
                    try {
                        assertEquals(5, fixture.getData(config));
                    } catch (InputOutputError inputOutputError) {
                        inputOutputError.printStackTrace();
                        fail("something failed");
                    }
                });

        File expectedFile = new File(String.format("%s%s%s_monthly_usage", testDir.getAbsolutePath(), File.separator,
            new SimpleDateFormat("yyyyMM").format(new Date())));
        assertTrue(expectedFile.exists());
        assertEquals("4", Files.readString(expectedFile.toPath()));
        assertThrows(InputOutputError.class, () -> fixture.getData(config));
        assertThrows(InputOutputError.class, () -> fixture.getData(config));

        verify(geoCodingApiProxy, times(4)).geocode(any(GeoApiContext.class), eq(String.format("%s %s, %s %s Germany",
                config.parts().get(6).value().get(), config.parts().get(9).value().get(),
                config.parts().get(7).value().get(), config.parts().get(8).value().get())));
        verifyNoMoreInteractions(geoCodingApiProxy);

        assertEquals(addressComponents[0].longName, config.parts().get(2).value().get());
        assertEquals(addressComponents[1].longName, config.parts().get(3).value().get());
        assertEquals(addressComponents[2].longName, config.parts().get(0).value().get());
        assertEquals(addressComponents[3].longName, config.parts().get(4).value().get());
        assertEquals(addressComponents[4].longName, config.parts().get(1).value().get());
        assertEquals(String.format("%d,%d", Math.round(geometry.location.lng), Math.round(geometry.location.lat)),
                config.parts().get(5).value().get());
    }

    private AddressComponent createComponent(final AddressComponentType type) {
        return new AddressComponent() {{
            longName = random(type.toString());
            types = new AddressComponentType[] { type };
        }};
    }

    private SimpleMappingPart createPart(final SimpleMappingConfiguration config, final String path,
                                         final boolean setValue) {
        return new SimpleMappingPart(config, new SimpleMappingPoint(random(), path),
                new SimpleMappingPoint(random(), path)) {{
            if (setValue) {
                getDataset().collect(new SimpleDatasetPiece(random(path)));
            }
        }};
    }

}