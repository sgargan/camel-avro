package org.apache.camel.component.avro.transport;

import org.apache.avro.ipc.*;
import org.apache.camel.component.avro.AvroConfiguration;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.eclipse.jetty.util.log.Log;

import java.net.InetSocketAddress;

public class NettyTransportProvider implements TransportProvider {

    private static final org.apache.commons.logging.Log LOG = LogFactory.getLog(NettyTransportProvider.class);
    
    public String getProviderType() {
        return "netty";
    }

    public Server getServerInstance(AvroConfiguration configuration, Responder responder) throws Exception  {
        InetSocketAddress endpointAddress = configuration.getEndpointAddress();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating Netty Server on '" + endpointAddress + "'");
        }
        Server server = new NettyServer(responder, endpointAddress);
        server.start();
        return server;
    }

    public Transceiver getTransceiverInstance(AvroConfiguration configuration) throws Exception {
        InetSocketAddress endpointAddress = configuration.getEndpointAddress();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating Netty Transceiver on '" + endpointAddress + "'");
        }
        return new NettyTransceiver(endpointAddress);
    }
    
   
}
