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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.avro.ipc.LocalTransceiver;
import org.apache.avro.ipc.Responder;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.Transceiver;
import org.apache.camel.Consumer;
import org.apache.camel.Producer;
import org.apache.camel.component.avro.AvroConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LocalTransportProvider implements TransportProvider {

    private static final Log LOG = LogFactory.getLog(LocalTransportProvider.class);

    public String getProviderType() {
        return "local";
    }

    public Server getServerInstance(AvroConfiguration configuration, Responder responder) throws Exception {
        // the local server uses the hostname/address from the uri to
        // correlate the transport between the producer and consumer
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating local transceiver for '" + configuration.getHost() + "'");
        }
        LocalTrancevierCache.storeTranceiver(configuration.getHost(), responder);
        return null;
    }

    public Transceiver getTransceiverInstance(AvroConfiguration configuration) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting local transceiver for '" + configuration.getHost() + "'");
        }
        return LocalTrancevierCache.getTransceiver(configuration.getHost());
    }

    private static final class LocalTrancevierCache {
        private static Map<String, DelegatingLocalTransceiver> transcievers = new ConcurrentHashMap<String, DelegatingLocalTransceiver>();

        private LocalTrancevierCache() { }
        
        public static void storeTranceiver(String host, Responder responder) {
            if (responder != null) {
                DelegatingLocalTransceiver transceiver = getDelegate(host);
                transceiver.setDelegate(new LocalTransceiver(responder));
            }
        }

        public static Transceiver getTransceiver(String host) {
            return getDelegate(host);
        }

        private static DelegatingLocalTransceiver getDelegate(String host) {
            DelegatingLocalTransceiver delegate = transcievers.get(host);
            if (delegate == null) {
                delegate = new DelegatingLocalTransceiver();
                transcievers.put(host, delegate);
            }
            return delegate;
        }
    }

    /**
     * DelegatingLocalTransceiver lets us create a {@link LocalTransceiver} for
     * use by a {@link Producer} before the target {@link Responder} has been
     * created in the corresponding {@link Consumer}, once the target is
     * available it is slotted in as the delegate.
     * 
     * @author sgargan
     */
    private static class DelegatingLocalTransceiver extends LocalTransceiver {
        LocalTransceiver delegate;

        public DelegatingLocalTransceiver() {
            super(null);
        }

        @Override
        public List<ByteBuffer> transceive(List<ByteBuffer> request) throws IOException {
            if (delegate == null) {
                throw new IllegalStateException("The target responder has not been created yet. Routes containing LocalTransceivers must ensure "
                                                + "that the consumers are created before the producers to avoid this issue.");
            }
            return delegate.transceive(request);
        }

        public void setDelegate(LocalTransceiver delegate) {
            this.delegate = delegate;
        }

    }

}
