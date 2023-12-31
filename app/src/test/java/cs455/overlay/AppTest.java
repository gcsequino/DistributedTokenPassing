/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package cs455.overlay;

import org.junit.Test;

import cs455.overlay.wireformats.*;

import static org.junit.Assert.*;

import java.io.IOException;

public class AppTest {
    @Test public void appHasAGreeting() {
        App classUnderTest = new App();
        assertNotNull("app should have a greeting", classUnderTest.getGreeting());
    }

    @Test public void ConnectionsDirective() throws IOException {
        ConnectionsDirective cn0 = new ConnectionsDirective(69, "0.0.0.0", 1234);
        byte[] bytes = cn0.marshall();
        ConnectionsDirective cn1 = new ConnectionsDirective(bytes);
        assertTrue(cn0.node_id == cn1.node_id && cn0.node_ip.equals(cn1.node_ip) && cn0.node_port == cn1.node_port);
    }

    @Test public void DataTraffic() throws IOException {
        DataTraffic dt0 = new DataTraffic(69, 123456789);
        byte[] bytes = dt0.marshall();
        DataTraffic dt1 = new DataTraffic(bytes);
        assertTrue(dt0.node_id == dt1.node_id && dt0.payload == dt1.payload);
    }

    @Test public void Deregister() throws IOException {
        Deregister d0 = new Deregister("0.0.0.0", 1234);
        byte[] bytes = d0.marshall();
        Deregister d1 = new Deregister(bytes);
        assertTrue(d0.ip_address.equals(d1.ip_address) && d0.port_number == d1.port_number);
    }

    @Test public void RegisterRequest() throws IOException {
        RegisterRequest rr0 = new RegisterRequest("0.0.0.0", 1234);
        byte[] bytes = rr0.marshall();
        RegisterRequest rr1 = new RegisterRequest(bytes);
        assertTrue(rr0.ip_address.equals(rr1.ip_address) && rr0.port_number == rr1.port_number);
    }

    @Test public void RegisterResponse() throws IOException {
        RegisterResponse rr0 = new RegisterResponse((byte)1, 69, "phrog");
        byte[] bytes = rr0.marshall();
        RegisterResponse rr1 = new RegisterResponse(bytes);
        assertTrue(rr0.status_code == rr1.status_code && rr0.node_id == rr1.node_id && rr0.info.equals(rr1.info));
    }

    @Test public void EventFactory() throws IOException {
        byte[] bytes = new ConnectionsDirective(69, "0.0.0.0", 1234).marshall();
        Event new_event = EventFactory.newEvent(bytes);
        assertTrue(new_event.get_message_type() == Protocol.CONNECTIONS_DIRECTIVE);
    }
}
