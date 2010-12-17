/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.avro;

import org.apache.avro.Protocol;
import org.apache.camel.Endpoint;
import org.apache.camel.component.avro.transport.TransportProvider;
import org.apache.camel.impl.DefaultComponent;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AvroComponent extends DefaultComponent {

    private AvroTransportFactory transportFactory;

    private final static String transportPackage = "org.apache.camel.component.avro.transport";

    private List<String> transportPackages = new ArrayList<String>();

    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        AvroEndpoint avroEndpoint = null;

        AvroConfiguration configuration = new AvroConfiguration();
        configuration.setProtocol(resolveAndRemoveReferenceParameter(parameters, "protocol", Protocol.class));
        configuration.setMessageName(getAndRemoveParameter(parameters, "message", String.class));
        //configuration.setShouldForwardRequest(getAndRemoveParameter(parameters, "shouldForwardRequest", Boolean.class, false));
        configuration.setShouldTransmitHeaders(getAndRemoveParameter(parameters, "forwardHeaders", Boolean.class, false));

        URI serverUri = new URI(remaining);
        configuration.setHost(serverUri.getHost());
        configuration.setPort(serverUri.getPort());
        configuration.setPath(serverUri.getPath());
        configuration.setEndpointType(serverUri.getScheme());

        avroEndpoint = new AvroEndpoint(uri, this, transportFactory, configuration);
        return avroEndpoint;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        transportPackages.add(transportPackage);
        Set<Class<?>> providers = getCamelContext().getPackageScanClassResolver().findImplementations(
                TransportProvider.class, transportPackages.toArray(new String[transportPackages.size()]));

        if (providers.isEmpty()) {
            throw new IllegalStateException("No transport providers were loaded from the" +
                    " configured transport packages '" + transportPackages + "'");
        }
        this.transportFactory = new AvroTransportFactory(providers);

    }

    public AvroTransportFactory getTransportFactory() {
        return transportFactory;
    }

    public void addTransportPackage(String transportPackage) {
        transportPackages.add(transportPackage);
    }

}
