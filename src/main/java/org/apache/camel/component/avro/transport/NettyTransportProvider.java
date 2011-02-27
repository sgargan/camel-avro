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

package org.apache.camel.component.avro.transport;

import java.net.InetSocketAddress;

import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.Responder;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.Transceiver;
import org.apache.camel.component.avro.AvroConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NettyTransportProvider implements TransportProvider {

    private static final Log LOG = LogFactory.getLog(NettyTransportProvider.class);

    public String getProviderType() {
        return "netty";
    }

    public Server getServerInstance(AvroConfiguration configuration, Responder responder) throws Exception {
        InetSocketAddress endpointAddress = configuration.getEndpointAddress();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating Netty Server on '" + endpointAddress + "'");
        }
        Server server = new NettyServer(responder, endpointAddress);
        server.start();
        return server;
    }

    public Transceiver getTransceiverInstance(AvroConfiguration configuration) throws Exception {
        InetSocketAddress endpointAddress = configuration.getEndpointAddress();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating Netty Transceiver on '" + endpointAddress + "'");
        }
        return new NettyTransceiver(endpointAddress);
    }

}
