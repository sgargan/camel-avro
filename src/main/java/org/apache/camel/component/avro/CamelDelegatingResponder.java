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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.avro.Protocol;
import org.apache.avro.ipc.Responder;
import org.apache.avro.ipc.Transceiver;
import org.apache.camel.InOut;

public class CamelDelegatingResponder {

    private ConnectionMetadata connectionMetadata = new ConnectionMetadata();
    private Responder responder;
    private boolean stateful;

    public CamelDelegatingResponder(Responder responder, boolean stateful) {
        this.responder = responder;
        this.stateful = stateful;
    }

    public CamelDelegatingResponder(Responder responder) {
        this(responder, false);
    }

    public void setResponder(Responder responder) {
        this.responder = responder;
    }

    public void setStateful(boolean stateful) {
        this.stateful = stateful;
    }

    @InOut
    public byte[] respond(List<ByteBuffer> buffers) throws IOException {
        List<ByteBuffer> response = responder.respond(buffers, stateful ? connectionMetadata : null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeBuffers(response, baos);
        return baos.toByteArray();
    }

    private void writeBuffers(List<ByteBuffer> buffers, OutputStream out) throws IOException {
        for (ByteBuffer buffer : buffers) {
            buffer.position(0);
            writeLength(buffer.limit(), out); // length-prefix
            out.write(buffer.array(), buffer.position(), buffer.remaining());
            buffer.position(buffer.limit());
        }
        writeLength(0, out); // null-terminate
    }

    private static void writeLength(int length, OutputStream out) throws IOException {
        System.out.println(length);
        out.write(0xff & (length >>> 24));
        out.write(0xff & (length >>> 16));
        out.write(0xff & (length >>> 8));
        out.write(0xff & length);
    }

    private static class ConnectionMetadata extends Transceiver {

        Protocol protocol;

        @Override
        public void setRemote(Protocol protocol) {
            this.protocol = protocol;
        }

        @Override
        public boolean isConnected() {
            return protocol != null;
        }

        @Override
        public String getRemoteName() {
            return protocol != null ? protocol.getName() : null;
        }

        @Override
        public List<ByteBuffer> readBuffers() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void writeBuffers(List<ByteBuffer> buffers) throws IOException {
            throw new UnsupportedOperationException();
        }
    }

}
