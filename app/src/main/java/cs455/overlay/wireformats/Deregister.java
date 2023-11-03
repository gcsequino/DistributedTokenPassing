package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Deregister extends Event {
    public int message_type = Protocol.DERGEDISTER_REQUEST;
    public String ip_address;
    public int port_number;

    public Deregister(String ip, int port) {
        ip_address = ip;
        port_number = port;
    }

    public Deregister(byte[] bytes) throws IOException {
        ByteArrayInputStream bArrayInputStream = new ByteArrayInputStream(bytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bArrayInputStream));

        din.readInt(); // take message_type out of stream
        this.ip_address = din.readUTF();
        this.port_number = din.readInt();

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
        dout.writeUTF(this.ip_address);
        dout.writeInt(this.port_number);

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

    public String get_ip() {
        return this.ip_address;
    }

    public int get_port() {
        return this.port_number;
    }
}