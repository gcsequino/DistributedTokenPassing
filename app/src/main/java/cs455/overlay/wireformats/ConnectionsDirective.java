package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ConnectionsDirective extends Event{
    public int message_type = Protocol.CONNECTIONS_DIRECTIVE;
    public int node_id;
    public String node_ip;
    public int node_port;

    public ConnectionsDirective(int node_id, String node_ip, int node_port) {
        this.node_id = node_id;
        this.node_ip = node_ip;
        this.node_port = node_port;
    }

    public ConnectionsDirective(byte[] bytes) throws IOException {
        ByteArrayInputStream bArrayInputStream = new ByteArrayInputStream(bytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bArrayInputStream));

        din.readInt(); // take message_type out of stream
        this.node_id = din.readInt();
        this.node_ip = din.readUTF();
        this.node_port = din.readInt();

        bArrayInputStream.close();
        din.close();
    }

    @Override
    public byte[] marshall() throws IOException {
        // setup
        byte[] marshalledBytes = null;
        ByteArrayOutputStream bArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(bArrayOutputStream));
        
        // write data
        dout.writeInt(this.message_type);
        dout.writeInt(this.node_id);
        dout.writeUTF(this.node_ip);
        dout.writeInt(this.node_port);

        // cleanup
        dout.flush();
        marshalledBytes = bArrayOutputStream.toByteArray();
        bArrayOutputStream.close();
        dout.close();
        return marshalledBytes;
    }

    @Override
    public int get_message_type() {
        return this.message_type;
    }

    public int get_id() {
        return this.node_id;
    }

    public String get_ip() {
        return this.node_ip;
    }

    public int get_port() {
        return this.node_port;
    }
}