package org.apache.camel.component.avro;

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

import java.io.IOException;

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

            if(transmitHeaders){
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
