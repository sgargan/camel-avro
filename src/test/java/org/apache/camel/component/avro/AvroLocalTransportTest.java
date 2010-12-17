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

import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.Transceiver;
import org.apache.camel.builder.RouteBuilder;

public class AvroLocalTransportTest extends AbstractAvroTransportTest {

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("avro:local://localOrdering/?protocol=orderingService").to("mock:order");
            }
        };
    }
    public String getEndpointUri(int port) {
        return "avro:local://localOrdering/?message=submitOrder&protocol=orderingService";
    }

    public Transceiver createTransceiver(int port) {
        return null;
    }
    
    public Server createServer(int port, MockOrderingService mock) throws IOException {
        return null;
    }
    
    @Override
    public void camelClientToAvroServer() throws Exception {
        // not really applicable
    }
    
    @Override
    public void avroClientToCamelServer() throws Exception {
        // not really applicable
    }
}
