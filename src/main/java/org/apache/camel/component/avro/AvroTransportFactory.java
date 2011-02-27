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

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.Responder;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.Transceiver;
import org.apache.camel.component.avro.transport.TransportProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>AvroTransportFactory</code> creates instances of Avro {@link Server}s
 * and client side {@link Transceiver}s.
 * <p>
 * It uses the transport type extracted from the endpoint uri to select the
 * correct {@link TransportProvider} from the set loaded at startup by the
 * {@link AvroComponent}
 */
public class AvroTransportFactory {

    private Logger log = LoggerFactory.getLogger(getClass());
    private Map<String, TransportProvider> providers = new ConcurrentHashMap<String, TransportProvider>();

    @SuppressWarnings("all")
    public AvroTransportFactory(Set<Class<?>> providerClasses) {
        for (Class providerClass : providerClasses) {
            try {
                if (!(providerClass.isInterface() || Modifier.isAbstract(providerClass.getModifiers()))) {
                    TransportProvider provider = (TransportProvider)providerClass.newInstance();
                    providers.put(provider.getProviderType(), provider);
                    if (log.isDebugEnabled()) {
                        log.debug("Created instance of '" + providerClass.getSimpleName() + "' transport provider.");
                    }
                }
            } catch (Exception e) {
                log.warn("Error creating provider from " + providerClass.getName(), e);
            }
        }
    }

    public Server getServerInstance(AvroConfiguration configuration, Responder responder) throws Exception {
        return getTransportProvider(configuration).getServerInstance(configuration, responder);
    }

    public Transceiver getTransceiverInstance(AvroConfiguration configuration) throws Exception {
        return getTransportProvider(configuration).getTransceiverInstance(configuration);
    }

    private TransportProvider getTransportProvider(AvroConfiguration configuration) throws AvroRemoteException {
        TransportProvider provider = providers.get(configuration.getTransportType());
        if (provider == null) {
            throw new AvroRemoteException("Unkown transport type '" + configuration.getTransportType() + "'");
        }
        return provider;
    }
}
