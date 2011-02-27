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

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.dataformat.avro.generated.Confirmation;
import org.apache.camel.dataformat.avro.generated.Order;
import org.apache.camel.util.ExchangeHelper;
import org.junit.Test;

public class AvroHeaderForwardingTest extends AbstractAvroComponentTest {

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("avro:netty://0.0.0.0:4567/?protocol=orderingService&forwardHeaders=true").to("mock:order");
            }
        };
    }

    @Test
    public void testHeaderTransmission() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:order");
        haveMockReplyToOrder(mock);

        Order order = createOrder();
        Exchange e = createExchangeWithBody(order);
        e.getIn().setHeader("this header", "gets transmitted as metadata");
        template.send("avro:netty://0.0.0.0:4567/?message=submitOrder&protocol=orderingService&forwardHeaders=true", e);

        Confirmation c = ExchangeHelper.getMandatoryOutBody(e, Confirmation.class);
        assertNotNull(e.getOut().getHeader("this header"));
        validateConfirmationCorrectlyRecieved(order, c);
        validateHeaderTransmission(mock, e);
    }

    protected void haveMockReplyToOrder(MockEndpoint mock) {
        mock.whenAnyExchangeReceived(new Processor() {
            public void process(Exchange e) throws Exception {
                Order[] order = e.getIn().getBody(Order[].class);
                e.getOut().setBody(new TestArtifacts.ConfirmationBuilder().forOrder(order[0]).build());
                e.getOut().setHeader("that header", "also gets transmitted as metadata");
            }
        });
    }

    private void validateHeaderTransmission(MockEndpoint mock, Exchange e) {
        assertNotNull(mock.assertExchangeReceived(0).getIn().getHeader("this header"));
    }

}
