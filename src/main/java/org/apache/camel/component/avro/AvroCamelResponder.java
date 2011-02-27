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

import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Protocol;
import org.apache.avro.Protocol.Message;
import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.Encoder;
import org.apache.avro.ipc.Responder;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.camel.Exchange;

/**
 * <code>AvroCamelResponder</code>
 */
public class AvroCamelResponder extends Responder {

    private AvroConsumer consumer;
    private boolean transmitHeaders;

    protected AvroCamelResponder(AvroConsumer consumer, Protocol protocol, boolean transmitHeaders) {
        super(protocol);
        this.consumer = consumer;
        this.transmitHeaders = transmitHeaders;
        if (transmitHeaders) {
            this.addRPCPlugin(new HeaderPackagingPlugin(consumer.getEndpoint().getCamelContext().getTypeConverter()));
        }
    }

    protected DatumWriter<Object> getDatumWriter(Schema schema) {
        return new SpecificDatumWriter<Object>(schema);
    }

    protected DatumReader<Object> getDatumReader(Schema schema) {
        return new SpecificDatumReader<Object>(schema);
    }

    @Override
    public Object readRequest(Schema schema, Decoder in) throws IOException {
        Object[] args = new Object[schema.getFields().size()];
        int i = 0;
        for (Schema.Field param : schema.getFields()) {
            args[i++] = getDatumReader(param.schema()).read(null, in);
        }
        return args;
    }

    @Override
    public void writeResponse(Schema schema, Object response, Encoder out) throws IOException {
        getDatumWriter(schema).write(response, out);
    }

    @Override
    public void writeError(Schema schema, Object error, Encoder out) throws IOException {
        getDatumWriter(schema).write(error, out);
    }

    @Override
    public Object respond(Message message, Object request) throws Exception {
        try {
            Exchange e = consumer.getEndpoint().createExchange();
            e.getIn().setHeader("CamelAvroMessageName", message.getName());
            e.getIn().setBody(request);

            if (transmitHeaders) {
                e.getIn().setHeaders(HeaderPackagingPlugin.getExchangeHeaders());
            }
            consumer.getProcessor().process(e);
            HeaderPackagingPlugin.setExchangeHeaders(e.getOut().getHeaders());
            return e.getOut().getBody();
        } catch (Exception e) {
            throw new AvroRuntimeException(e);
        }
    }
}
