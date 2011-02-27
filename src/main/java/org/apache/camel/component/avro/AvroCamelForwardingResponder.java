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

import java.nio.ByteBuffer;
import java.util.List;

import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Protocol;
import org.apache.avro.ipc.RPCContext;
import org.apache.avro.ipc.Transceiver;
import org.apache.camel.Exchange;

/**
 * <code>AvroCamelForwardingResponder</code> is a
 * {@link AvroForwardingResponder} responder that uses Camel as the forwarding
 * conduit. It
 */
public class AvroCamelForwardingResponder extends AvroForwardingResponder {

    private AvroConsumer consumer;

    public AvroCamelForwardingResponder(AvroConsumer consumer, Protocol protocol) {
        super(protocol);
        this.consumer = consumer;
    }

    @Override
    protected List<ByteBuffer> forward(List<ByteBuffer> buffers, Transceiver connection, Protocol remote, RPCContext metadata) {
        try {
            Exchange e = consumer.getEndpoint().createExchange();
            // TODO: determine if protocol hash is enough here
            e.getIn().setHeader("CamelAvroForwardedRequest", remote);
            e.getIn().setHeader("CamelAvroMessageName", metadata.getMessage().getName());
            e.getIn().setBody(buffers);
            consumer.getProcessor().process(e);
            return (List<ByteBuffer>)e.getOut().getBody();
        } catch (Exception e) {
            throw new AvroRuntimeException(e);
        }
    }

}
