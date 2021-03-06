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

import java.io.IOException;

import org.apache.avro.Protocol;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.camel.Exchange;
import org.apache.camel.component.avro.transport.NettyTransportProvider;
import org.apache.camel.impl.DefaultProducer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AvroProducer extends DefaultProducer {
    private static final Log LOG = LogFactory.getLog(NettyTransportProvider.class);

    private CamelRequestor requestor;
    private boolean shouldTransmitHeaders;
    private AvroConfiguration configuration;
    private Transceiver transceiver;

    public AvroProducer(AvroEndpoint endpoint) {
        super(endpoint);
        this.configuration = endpoint.getConfiguration();
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        transceiver = getTransportFactory().getTransceiverInstance(configuration);
        requestor = new CamelRequestor(configuration.getProtocol(), transceiver);

        shouldTransmitHeaders = configuration.shouldTransmitHeaders();
        if (shouldTransmitHeaders) {
            requestor.addRPCPlugin(new HeaderPackagingPlugin(getEndpoint().getCamelContext().getTypeConverter()));
        }
    }

    @SuppressWarnings("all")
    public void process(Exchange exchange) throws Exception {
        if (shouldTransmitHeaders) {
            HeaderPackagingPlugin.setExchangeHeaders(exchange.getIn().getHeaders());
        }
        Object payload = exchange.getIn().getBody();
        if (!(payload instanceof Object[])) {
            payload = new Object[] {payload};
        }

        Object response = requestor.request(getMessageName(exchange), payload);
        exchange.getOut().setBody(response);

        if (shouldTransmitHeaders) {
            exchange.getOut().setHeaders(HeaderPackagingPlugin.getExchangeHeaders());
        }

    }

    private String getMessageName(Exchange exchange) {
        String messageName = exchange.getIn().getHeader(AvroEndpoint.MESSAGE_NAME, String.class);
        if (messageName == null) {
            messageName = configuration.getMessageName();
        }
        if (messageName == null) {
            log.warn("No target message operation has been specified. If the receiver is an avro endpoint this remote call will fail");
        }
        return messageName;
    }

    private AvroTransportFactory getTransportFactory() {
        return ((AvroEndpoint)getEndpoint()).getTranportFactory();
    }

    public static class CamelRequestor extends SpecificRequestor {

        public CamelRequestor(Protocol protocol, Transceiver transceiver) throws IOException {
            super(protocol, transceiver);
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        try {
            transceiver.close();
        } catch (Exception e) {
            LOG.error("Error closing transceiver '" + transceiver.getClass().getSimpleName() + "'");
        }
    }

}
