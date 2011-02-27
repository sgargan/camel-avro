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

import org.apache.avro.ipc.Responder;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.Transceiver;
import org.apache.camel.component.avro.AvroConfiguration;

/**
 * TransportProvider abstracts the creation of specific Avro IPC transport
 * mechanisms for endpoints.
 */
public interface TransportProvider {

    /**
     * Gets the type of ipc mechanism that this provider creates. This is used
     * to match against the type extracted from the avro endpoint.
     * 
     * @return the type of ipc mechanism to create.
     */
    String getProviderType();

    /**
     * Uses the given configuration to create an appropriate {@link Server}
     * instance for the route.
     * 
     * @param configuration
     * @param responder
     * @return
     * @throws Exception
     */
    Server getServerInstance(AvroConfiguration configuration, Responder responder) throws Exception;

    /**
     * Uses the given configuration to create a {@link Transceiver} appropriate
     * for the route.
     * 
     * @param configuration
     * @return
     * @throws Exception
     */
    Transceiver getTransceiverInstance(AvroConfiguration configuration) throws Exception;

}
