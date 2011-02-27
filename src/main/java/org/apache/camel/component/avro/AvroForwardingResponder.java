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
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Protocol;
import org.apache.avro.Protocol.Message;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.ipc.HandshakeRequest;
import org.apache.avro.ipc.RPCContext;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.generic.GenericResponder;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.util.ByteBufferInputStream;


/**
 * <code>AvroForwardingResponder</code> is a preprocessing Responder that
 * extracts the {@link Protocol} from an inbound request in order route the
 * request based on examination of the {@link Protocol} and the request
 * contents. The entire request is forwarded to an upstream handler for
 * processing. Only the data that is required to route the message (if any) need
 * be decoded in this responder, by default this means the {@link Protocol} and
 * the target message.
 */
public abstract class AvroForwardingResponder extends GenericResponder {
    
    private static final Schema META = Schema.createMap(Schema.create(Schema.Type.BYTES));
    
    private static final GenericDatumReader<Map<CharSequence, ByteBuffer>> META_READER = new GenericDatumReader<Map<CharSequence, ByteBuffer>>(META);

    private SpecificDatumReader<HandshakeRequest> handshakeReader = new SpecificDatumReader<HandshakeRequest>(HandshakeRequest.class);

    protected AvroForwardingResponder(Protocol protocol) {
        super(protocol);
    }

    @Override
    public List<ByteBuffer> respond(List<ByteBuffer> buffers, Transceiver connection) throws IOException {
        Decoder in = DecoderFactory.defaultFactory().createBinaryDecoder(new ByteBufferInputStream(buffers), null);
        Protocol remote = readProtocolFromHandshake(in, connection);
        RPCContext context = readCallMetaData(in, remote);
        return forward(buffers, connection, remote, context);
    }

    /**
     * Delegate the processing of a request to a subclass which can make routing
     * and processing decisions based on the Protocol and buffer contents. The
     * result on the upstream processing is passed back unchanged so the
     * upstream processing must take responsibility for finishing the handshake.
     * 
     * @param buffers the buffers constituting the original request being
     *            forwarded
     * @param connection the current client connection
     * @param remote the protocol associated with the connection.
     * @param context containing metadata about the rpc call.
     * @return the buffers as processed by the upstream responder or handler
     */
    protected abstract List<ByteBuffer> forward(List<ByteBuffer> buffers, Transceiver connection, Protocol remote, RPCContext context);

    @Override
    public Object respond(Message message, Object request) throws Exception {
        throw new UnsupportedOperationException();
    }

    private Protocol readProtocolFromHandshake(Decoder in, Transceiver connection) throws IOException {
        if (connection != null && connection.isConnected()) {
            return connection.getRemote();
        }
        // HandshakeRequest request = handshakeReader.read(null, in);
        // Protocol protocol =
        // ProtocolsCache.getCachedProtocol(request.clientHash);
        // if (protocol == null && request.clientProtocol != null) {
        // protocol = Protocol.parse(request.clientProtocol.toString());
        // ProtocolsCache.register(protocol);
        // }
        // connection.setRemote(protocol);
        return null;
    }

    private RPCContext readCallMetaData(Decoder in, Protocol remote) throws IOException {
        RPCContext context = new MetaDataAccessibleRPCContext(META_READER.read(null, in));
        String messageName = in.readString(null).toString();
        Message rm = remote.getMessages().get(messageName);
        if (rm == null) {
            throw new AvroRuntimeException("No such remote message: " + messageName);
        }
        context.setMessage(rm);
        return context;
    }

    private static class MetaDataAccessibleRPCContext extends RPCContext {

        public MetaDataAccessibleRPCContext(Map<CharSequence, ByteBuffer> callMetadata) {
            this.requestCallMeta = callMetadata;
            for (CharSequence key : callMetadata.keySet()) {
                System.err.println(key);
            }
        }
    }
}
