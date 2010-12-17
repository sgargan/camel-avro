package org.apache.camel.component.avro.transport;

import org.apache.avro.ipc.Responder;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.Transceiver;
import org.apache.camel.component.avro.AvroConfiguration;

/**
 * TransportProvider abstracts the creation of specific Avro IPC transport
 * mechanisms for endpoints.
 */
public interface TransportProvider {

    /**
     * Gets the type of ipc mechanism that this provider creates. This is used
     * to match against the type extracted from the avro endpoint.
     * 
     * @return the type of ipc mechanism to create.
     */
    String getProviderType();

    Server getServerInstance(AvroConfiguration configuration, Responder responder) throws Exception;

    Transceiver getTransceiverInstance(AvroConfiguration configuration) throws Exception;
}
