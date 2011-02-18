package org.apache.camel.component.avro;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.avro.Protocol;
import org.apache.avro.ipc.RPCPlugin;

public class AvroConfiguration {

    public String host;
    public Integer port = 4567;
    private Protocol protocol;
    private RPCPlugin plugin;
    private boolean propagateHeaders;
    private Class<?> serviceInterface;
    private String messageName;
    private boolean shouldForwardRequest;
    private String endpointType;
    private String path;

    public AvroConfiguration() {
        host = "0.0.0.0";
    }

    public String getHost() {
        return host;
    } 
    
    public void setHost(String host) {
        this.host = host;
    }

    public InetSocketAddress getEndpointAddress() throws Exception {
        return new InetSocketAddress(InetAddress.getByName(host), port);
    }

    public void setPath(String path) {
        this.path = path;
    }
    
    public String getPath() {
        return path;
    }
    /**
     * Sets the port generally as extracted from the endpoint uri
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public RPCPlugin getRPCPlugin() {
        return plugin;
    }

    public void setPlugin(RPCPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Determines if the headers from the exchange should be included as
     * metadata headers in the RPC communication
     * 
     * @param propagateHeaders
     */
    public void setShouldTransmitHeaders(boolean propagateHeaders) {
        this.propagateHeaders = propagateHeaders;
    }

    public boolean shouldTransmitHeaders() {
        return propagateHeaders;
    }

    /**
     * gets the target RPC interface as specified by the
     * 
     * @return
     */
    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    /**
     * Gets the configured 'message name', the target operation that should be
     * executed on the service. This value can be dynamically set on a per
     * exchange basis by setting the header {@link AvroEndpoint}.MESSAGE_NAME in
     * the header with the target message name
     * 
     * @return the configured target message or null if none has been
     *         configured.
     */
    public String getMessageName() {
        return messageName;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    /**
     * Determines if the endpoint should only extract routing information from
     * the request and forward it on for processing further upstream in the
     * route.
     * <p>
     * If true a forwarding responder is used to to process the inbound request.
     * It will extract metadata about the rpc call from the request, such as the
     * protocol and the rpc message name, storing it in exchange headers for use
     * in routing to the upstream destination endpoint.
     * 
     * @return true if the endpoint should only extract routing information from
     *         the request and forward it
     */
    public boolean shouldForwardRequest() {
        return shouldForwardRequest;
    }

    public void setShouldForwardRequest(boolean shouldForwardRequest) {
        this.shouldForwardRequest = shouldForwardRequest;
    }

    /**
     * Gets the type of the transport that should be used for.
     * 
     * @return
     */
    public String getTransportType() {
        return endpointType;
    }

    public void setEndpointType(String endpointType) {
        this.endpointType = endpointType;
    }

}
