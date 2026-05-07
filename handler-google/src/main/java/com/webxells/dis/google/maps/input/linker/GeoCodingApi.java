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
import com.google.maps.errors.ApiException;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.GeocodingResult;
import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.input.linker.JoinLinker;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.base.resource.StringResource;
import com.webxells.dis.gis.PointType;
import com.webxells.dis.gis.coordinate.Xy;
import com.webxells.dis.gis.coordinate.parser.XyParser;
import com.webxells.dis.google.GoogleServiceConfig;
import com.webxells.dis.google.maps.internal.GeoCodingApiProxy;
import com.webxells.dis.logging.LoggerProxyFactory;
import com.webxells.dis.plain.output.Echo;
import com.webxells.dis.plain.output.EchoConfig;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Description("Requesting Google Maps Geocoding service (address -> coordinates")
public class GeoCodingApi implements JoinLinker {
    private enum FileOperation {
        FILE_READ, FILE_WRITE
    }

    private final static SimpleDateFormat FILE_PATTERN = new SimpleDateFormat("yyyyMM'_monthly_usage'");
    private final static Logger LOGGER = LoggerProxyFactory.logger(GeoCodingApi.class);

    private final String inputName = String.valueOf(System.identityHashCode(this));
    private final GeoCodingApiProxy geoCodingApiProxy;
    @Required
    @Description("Defines how to send address; use plain.Echo template notation")
    private String template;
    @Required
    @Description("Specifies usage of the Google service")
    private GoogleServiceConfig google;
    @Description("Limits call amounts er months; used of usageSavePath is provided")
    @Default("0")
    @Required(ifPresent = "usageSavePath")
    private long maxUsesPerMonth = 0;
    private GeoApiContext geoApiContext;
    @Description("Path of directory where usages are saved; Asserts if present that maxUsesPerMonth is never overstepped ")
    private String usageSavePath;
    @Description("Defines parsing of geo coordinates")
    @Default("WHITE_SPACE")
    private PointType parseType = XyParser.DEFAULT_POINT_TYPE;


    public GeoCodingApi() { this(new GeoCodingApiProxy()); }

    GeoCodingApi(final GeoCodingApiProxy geoCodingApiProxy) {
        this.geoCodingApiProxy = geoCodingApiProxy;
    }

    private static synchronized long accurateFileOperation(final FileOperation operation, final String usageSavePath) throws InputOutputError {
        final File currentFile = createCurrentCountFile(usageSavePath);
        final long result = readLastCallCount(currentFile);
        if (FileOperation.FILE_WRITE == operation) {
            writeNewCallCount(currentFile, result + 1);
        }
        return result;
    }

    private static void writeNewCallCount(final File currentFile, final long value) throws InputOutputError {
        if (currentFile.exists() && !currentFile.canWrite()) {
            throw new InputOutputError("Not writable: ".concat(currentFile.getAbsolutePath()));
        }
        try {
            Files.write(currentFile.toPath(), String.valueOf(value).getBytes(), StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (final IOException e) {
            throw new InputOutputError("Could not write to restriction file: ".concat(currentFile.getAbsolutePath()), e);
        }
    }

    private static long readLastCallCount(final File currentFile) throws InputOutputError {
        if (currentFile.exists() && currentFile.isFile() && currentFile.canRead()) {
            try {
                return Long.parseLong(Files.readString(currentFile.toPath()));
            } catch (final IOException e) {
                throw new InputOutputError("Could not read from monthly usage file: ".concat(currentFile.getAbsolutePath()), e);
            } catch (final NumberFormatException e) {
                throw new InputOutputError("Not a number in usage file: ".concat(currentFile.getAbsolutePath()), e);
            }
        }
        return 0;
    }

    private static File createCurrentCountFile(final String usageSavePath) {
        return new File(String.format("%s%s%s", usageSavePath, File.separator , FILE_PATTERN.format(new Date())));
    }

    @Override
    public void validate() throws InvalidApi {
        if (null == template || null == google || null == google.getApiKey()) {
            throw new InvalidApi("required parameters not provided");
        }
        if (maxUsesPerMonth > 0 && null == usageSavePath) {
            throw new InvalidApi("no usage save path provided");
        }
    }

    @Override
    public int getData(final MappingConfiguration mappingConfiguration) throws InputOutputError {
        final long lastCallCount = accurateFileOperation(FileOperation.FILE_READ, usageSavePath);
        if (lastCallCount < maxUsesPerMonth) {
            LOGGER.trace(String.format("Current monthly google api call count: %d", lastCallCount));
            final int result;
            try {
                result = callGoogleApi(mappingConfiguration, composeAddress(mappingConfiguration));
            } catch (final InterruptedException | ApiException | IOException | DisException e) {
                throw new InputOutputError("Could not call google successfully", e);
            }
            accurateFileOperation(FileOperation.FILE_WRITE, usageSavePath);
            return result;
        }
        throw new InputOutputError("Monthly google service api call exceeded - wait till next month : >");
    }

    @Override
    public String getInputName() {
        return inputName;
    }

    public void setParseType(final PointType parseType) {
        this.parseType = parseType;
    }

    @Override
    public void start() {
        geoApiContext = new GeoApiContext.Builder()
                .connectTimeout(google.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(google.getReadTimeout(),  TimeUnit.MILLISECONDS)
                .maxRetries(google.getMaxRetries())
                .queryRateLimit(google.getQueryRateLimit())
                .retryTimeout(google.getRetryTimeout(),  TimeUnit.MILLISECONDS)
                .apiKey(google.getApiKey())
                .writeTimeout(google.getWriteTimeout(),  TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public void end() {
        geoApiContext.shutdown();
    }

    @Override
    public String getType() {
        return GeoCodingApi.class.getName();
    }

    public void setTemplate(final String template) {
        this.template = template;
    }

    public void setMaxUsesPerMonth(final long maxUsesPerMonth) {
        this.maxUsesPerMonth = maxUsesPerMonth;
    }

    public void setGoogle(final GoogleServiceConfig google) {
        this.google = google;
    }

    public void setUsageSavePath(final String usageSavePath) {
        this.usageSavePath = usageSavePath;
    }

    private int callGoogleApi(final MappingConfiguration mappingConfiguration, final String address) throws InterruptedException, ApiException, IOException {
        LOGGER.trace(String.format("Calling google coding api: %s", address));
        final GeocodingResult[] results = geoCodingApiProxy.geocode(geoApiContext, address).await();
        if (null == results || results.length < 1) {
            LOGGER.debug("Google found nothing");
            return 0;
        }
        if (results.length > 1) {
            LOGGER.trace("Got multiple results - using first");
        }
        return saveResultToConfig(results[0], mappingConfiguration);
    }

    private int saveResultToConfig(final GeocodingResult result, final MappingConfiguration mappingConfiguration) {
        final AtomicInteger count = new AtomicInteger();
        for (MappingPart part : mappingConfiguration.parts()) {
            if (null != part.getInput() && null != part.getInput().getPath()) {
                if ("coordinates".equals(part.getInput().getPath())) {
                    part.getDataset().collect(new SimpleDatasetPiece(XyParser.output(parseType,
                            new Xy(result.geometry.location.lng, result.geometry.location.lat, null))));
                } else {
                    lookForMappingComponentType(result, count, part);
                }
            }
        }
        return count.get();
    }

    private void lookForMappingComponentType(final GeocodingResult result, final AtomicInteger count, final MappingPart part) {
        for (AddressComponent component : result.addressComponents) {
            final Optional<AddressComponentType> any = Arrays.stream(component.types)
                    .filter(a -> part.getInput().getPath().equals(a.toString()))
                    .findAny();
            if (any.isPresent()) {
                part.getDataset().collect(new SimpleDatasetPiece(component.longName));
                count.incrementAndGet();
                return;
            }
        }
    }

    private String composeAddress(final MappingConfiguration mappingConfiguration) throws DisException {
        final EchoConfig echoConfig = new EchoConfig();
        echoConfig.setTemplate(template);
        echoConfig.setName(inputName);
        echoConfig.setSender(new StringResource());
        final Echo echo = new Echo(echoConfig);
        echo.start();
        echo.write(copyMapping(mappingConfiguration));
        echo.end();
        return echoConfig.getSender().send().toString();
    }

    private MappingConfiguration copyMapping(final MappingConfiguration mappingConfiguration) {
        final SimpleMappingConfiguration result = new SimpleMappingConfiguration();
        mappingConfiguration.parts().stream()
                .filter(a -> null != a.getOutput())
                .forEach(a -> result.addPart(new SimpleMappingPart(result, new SimpleMappingPoint(),
                        new SimpleMappingPoint(inputName, a.getOutput().getPath())) {{
                            a.value().ifPresent(b -> getDataset().collect(new SimpleDatasetPiece(b)));
                }}));
        return result;
    }
}