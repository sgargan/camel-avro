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
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class AvroRequestForwardingTest extends AbstractAvroComponentTest {

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from("avro:local://ordering?protocol=orderingService").to("mock:upstream");
                from("avro:netty://0.0.0.0:4567/?protocol=orderingService&shouldForwardRequest=true").to("mock:metadata", "avro:local://ordering?protocol=orderingService");
            }
        };
    }

    @Test
    public void metadataIsForwardedAsHeaders() throws Exception {

        Exchange e = createExchangeWithBody(createOrder());
        System.err.println("sending");
        template.send("avro:netty://0.0.0.0:4567/?message=submitOrder&protocol=orderingService", e);

        MockEndpoint mock = getMockEndpoint("mock:metadata");
        mock.await();

        Exchange received = mock.assertExchangeReceived(0);
    }
}
