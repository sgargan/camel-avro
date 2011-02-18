package org.apache.camel.component.avro;

import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Protocol;
import org.apache.avro.ipc.RPCContext;
import org.apache.avro.ipc.Transceiver;
import org.apache.camel.Exchange;

import java.nio.ByteBuffer;
import java.util.List;

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
            //TODO: determine if protocol hash is enough here
            e.getIn().setHeader("CamelAvroForwardedRequest", remote);
            e.getIn().setHeader("CamelAvroMessageName", metadata.getMessage().getName());
            e.getIn().setBody(buffers);
            consumer.getProcessor().process(e);
            return (List<ByteBuffer>) e.getOut().getBody();
        } catch (Exception e) {
            throw new AvroRuntimeException(e);
        }
    }

}
