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
package org.apache.camel.dataformat.avro;

import java.util.HashMap;

import org.apache.avro.Schema;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.dataformat.avro.generated.Item;
import org.apache.camel.dataformat.avro.generated.Order;
import org.apache.camel.dataformat.avro.generated.OrderingProcessingService;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class AvroDataFormatTest extends CamelTestSupport {

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                Schema schema = OrderingProcessingService.PROTOCOL.getTypes().iterator().next();
                AvroDataFormat format = new AvroDataFormat(schema);
                from("direct:roundtrip").marshal(format).unmarshal(format).to("mock:result");
            }
        };
    }

    @Test
    public void testRoundtripConversion() throws Exception {

        Order testOrder = createTestOrder();
        MockEndpoint mock = getMockEndpoint("mock:reverse");
        mock.expectedMessageCount(1);
        mock.message(0).body().isInstanceOf(Order.class);
        mock.message(0).body().equals(testOrder);

        template.sendBody("direct:roundtrip", testOrder);
        mock.assertIsSatisfied();
    }

    private Order createTestOrder() {
        Order input = new Order();
        input.customerId = 134;
        input.orderId = 4567;
        input.orderItems = new HashMap<CharSequence, Item>();
        input.orderItems.put("tastyBurger", createItem("tastyBurger", 6789, 1));
        return input;
    }

    private Item createItem(String name, int sku, int quantity) {
        Item item = new Item();
        item.name = name;
        item.sku = sku;
        item.quantity = quantity;
        return item;
    }
}
