package org.apache.camel.component.avro.transport;

import java.net.InetSocketAddress;

import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.Responder;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.Transceiver;
import org.apache.camel.component.avro.AvroConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NettyTransportProvider implements TransportProvider {

    private static final Log LOG = LogFactory.getLog(NettyTransportProvider.class);

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
