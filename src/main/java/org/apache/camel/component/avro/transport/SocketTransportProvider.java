package org.apache.camel.component.avro.transport;

import org.apache.avro.ipc.*;
import org.apache.camel.component.avro.AvroConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.InetSocketAddress;

public class SocketTransportProvider implements TransportProvider {

    private static final Log LOG = LogFactory.getLog(NettyTransportProvider.class);

    public String getProviderType() {
        return "socket";
    }

    public Server getServerInstance(AvroConfiguration configuration, Responder responder) throws Exception  {
        InetSocketAddress endpointAddress = configuration.getEndpointAddress();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating Socket Server on '" + endpointAddress + "'");
        }
        Server server = new SocketServer(responder, endpointAddress);
        server.start();
        return server;
    }

    public Transceiver getTransceiverInstance(AvroConfiguration configuration) throws Exception {
        InetSocketAddress endpointAddress = configuration.getEndpointAddress();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating Socket Transceiver on " + endpointAddress);
        }
        return new SocketTransceiver(endpointAddress);
    }
}
