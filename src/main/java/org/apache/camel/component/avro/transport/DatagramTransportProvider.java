package org.apache.camel.component.avro.transport;

import org.apache.avro.ipc.*;
import org.apache.camel.component.avro.AvroConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.InetSocketAddress;

public class DatagramTransportProvider implements TransportProvider {

    private static final Log LOG = LogFactory.getLog(DatagramTransportProvider.class);
    
    public String getProviderType() {
        return "datagram";
    }

    public Server getServerInstance(AvroConfiguration configuration, Responder responder) throws Exception  {
        InetSocketAddress endpointAddress = configuration.getEndpointAddress();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating Datagram Server on " + endpointAddress);
        }
        Server server = new DatagramServer(responder, endpointAddress);
        server.start();
        return server;
    }

    public Transceiver getTransceiverInstance(AvroConfiguration configuration) throws Exception {
        InetSocketAddress endpointAddress = configuration.getEndpointAddress();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating Datagram Transceiver on '" + endpointAddress+"'");
        }
        return new DatagramTransceiver(endpointAddress);
    }
}
