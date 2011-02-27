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

import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.dataformat.avro.generated.Confirmation;
import org.apache.camel.dataformat.avro.generated.Order;
import org.apache.camel.dataformat.avro.generated.OrderProcessingService;
import org.apache.camel.util.ExchangeHelper;
import org.junit.Test;

public abstract class AbstractAvroTransportTest extends AbstractAvroComponentTest {

    public abstract String getEndpointUri(int port);

    public abstract Transceiver createTransceiver(int port) throws Exception;

    public abstract Server createServer(int port, MockOrderingService mock) throws Exception;

    @Test
    public void avroClientToCamelServer() throws Exception {
        Order order = createOrder();
        MockEndpoint mock = getMockEndpoint("mock:order");
        haveMockReplyToOrder(mock);
        Transceiver t = null;

        try {
            t = createTransceiver(4567);
            OrderProcessingService service = SpecificRequestor.getClient(OrderProcessingService.class, t);
            for (int x = 0; x < 5; x++) {
                Confirmation c = service.submitOrder(order);
                validateConfirmationCorrectlyRecieved(order, c);
            }
        } finally {
            closeTransceiverQuietly(t);
        }
    }

    @Test
    public void camelClientToCamelServer() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:order");
        haveMockReplyToOrder(mock);

        Order order = createOrder();
        Exchange e = createExchangeWithBody(order);
        template.send(getEndpointUri(4567), e);

        Confirmation c = ExchangeHelper.getMandatoryOutBody(e, Confirmation.class);
        validateConfirmationCorrectlyRecieved(order, c);
    }

    @Test
    public void camelClientToAvroServer() throws Exception {

        Server server = null;
        try {
            MockOrderingService mock = new MockOrderingService();
            server = createServer(4568, mock);
            server.start();

            Order order = createOrder();
            Exchange e = createExchangeWithBody(order);
            template.send(getEndpointUri(4568), e);

            Confirmation c = ExchangeHelper.getMandatoryOutBody(e, Confirmation.class);
            validateConfirmationCorrectlyRecieved(order, c);

        } finally {
            if (server != null) {
                server.close();
            }
        }
    }
}
