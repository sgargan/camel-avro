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

import java.util.concurrent.CountDownLatch;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.Transceiver;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.avro.TestArtifacts.ConfirmationBuilder;
import org.apache.camel.component.avro.TestArtifacts.ItemBuilder;
import org.apache.camel.component.avro.TestArtifacts.OrderBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.dataformat.avro.generated.Confirmation;
import org.apache.camel.dataformat.avro.generated.Order;
import org.apache.camel.dataformat.avro.generated.OrderFailure;
import org.apache.camel.dataformat.avro.generated.OrderProcessingService;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;

public abstract class AbstractAvroComponentTest extends CamelTestSupport {

    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry reg = super.createRegistry();
        reg.bind("orderingService", OrderProcessingService.PROTOCOL);
        reg.bind("orderingServiceImpl", new MockOrderingService());
        return reg;
    }
    
    protected static Order createOrder() {
        return new OrderBuilder().addItems(new ItemBuilder().build()).build();
    }

    protected void haveMockReplyToOrder(MockEndpoint mock) {
        mock.whenAnyExchangeReceived(new Processor() {
            public void process(Exchange e) throws Exception {
                Order[] order = e.getIn().getBody(Order[].class);
                e.getOut().setBody(new ConfirmationBuilder().forOrder(order[0]).build());
            }
        });
    }

    protected void closeTransceiverQuietly(Transceiver t) {
        try {
            if (t.isConnected()) {
                t.close();
            }
        } catch (Exception e) {
        }
    }

    protected void validateConfirmationCorrectlyRecieved(Order order, Confirmation c) {
        assertEquals(order.orderId, c.orderId);
        assertEquals(order.customerId, c.customerId);
    }

    protected static class MockOrderingService implements OrderProcessingService {

        public Order received;
        public CountDownLatch latch = new CountDownLatch(1);

        public Confirmation submitOrder(Order order) throws AvroRemoteException, OrderFailure {
            received = order;
            latch.countDown();
            return new ConfirmationBuilder().forOrder(order).build();
        }

        public void await() throws InterruptedException {
            latch.await();
        }

        public void assertSatisfied() {
            assertEquals(received, createOrder());
        }
    }

}
