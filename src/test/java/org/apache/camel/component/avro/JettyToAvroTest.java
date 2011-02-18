package org.apache.camel.component.avro;

import java.net.URL;

import org.apache.avro.ipc.HttpTransceiver;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.avro.ipc.specific.SpecificResponder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.dataformat.avro.generated.Confirmation;
import org.apache.camel.dataformat.avro.generated.Order;
import org.apache.camel.dataformat.avro.generated.OrderProcessingService;
import org.apache.camel.impl.JndiRegistry;
import org.junit.Test;

public class JettyToAvroTest extends AbstractAvroComponentTest {

    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry reg = super.createRegistry();
        reg.bind("testResponder", new CamelDelegatingResponder(new SpecificResponder(OrderProcessingService.PROTOCOL, new MockOrderingService())));
        return reg;
    }
    
    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() { 
            public void configure() {
                from("jetty:http://0.0.0.0:4567/jetty-endpoint").to("bean:testResponder?method=respond");
            }
        };
    }

    @Test
    public void anyCamelEndpointType() throws Exception {
        Order order = createOrder();
        MockEndpoint mock = getMockEndpoint("mock:order");
        haveMockReplyToOrder(mock);
        Transceiver t = null;


        try {
            t = new HttpTransceiver(new URL("http://0.0.0.0:4567/jetty-endpoint"));
            OrderProcessingService service = SpecificRequestor.getClient(OrderProcessingService.class, t);
            for(int x = 0 ; x < 5; x++){
                Confirmation c = service.submitOrder(order);
                validateConfirmationCorrectlyRecieved(order, c);
            }
        } finally {
            closeTransceiverQuietly(t);
        }
    }
}

