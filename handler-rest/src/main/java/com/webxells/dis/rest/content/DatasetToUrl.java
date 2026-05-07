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

import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.rest.RestConfig;
import com.webxells.dis.rest.execution.HttpMethod;
import com.webxells.dis.rest.execution.Request;
import com.webxells.dis.rest.execution.Response;
import com.webxells.dis.rest.url.StandardUriQuery;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Description("Writes Output result to baseUrl")
public class DatasetToUrl extends DatasetTo {
    @Description("Which encoding to use for the url parameters")
    @Default("UTF_8")
    private String parameterEncoding = StandardCharsets.UTF_8.name();
    @Description("skip url escaping")
    @Default("false")
    private boolean useAsRawUri;

    @Override
    public void parseInputRequest(final HttpMethod method, final Request.Builder builder,
                                  final RestConfig restConfig) {
        final URI uri = URI.create(getGeneratedOutput());
        builder.uri(uri);
        Optional.ofNullable(restConfig)
                .ifPresent(a -> a.setBaseUrl(uri.toString()));
    }

    @Override
    public InputStream parseOutputRequest(final Response response)  {
        return new ByteArrayInputStream(response.getUri().toString().getBytes());
    }

    public void setParameterEncoding(final String parameterEncoding) {
        this.parameterEncoding = parameterEncoding;
    }

    @Override
    public void setContent(final Object content) {
        super.setContent(content);
        if (!useAsRawUri) {
            rootDataset = rootDataset.copy();
            rootDataset.parts()
                    .forEach(a -> a.value().ifPresent(b -> {
                        a.getDataset().clear();
                        a.getDataset().collect(new SimpleDatasetPiece(StandardUriQuery.encode(b, parameterEncoding)));
                    }));
        }
    }

    @Override
    public String getType() {
        return DatasetToUrl.class.getName();
    }

    public void setUseAsRawUri(final boolean useAsRawUri) {
        this.useAsRawUri = useAsRawUri;
    }
}