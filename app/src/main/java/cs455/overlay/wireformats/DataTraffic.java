package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DataTraffic extends Event {
    public int message_type = Protocol.DATA_TRAFFIC;
    public int node_id;
    public int payload;

    public DataTraffic(int node_id, int payload) {
        this.node_id = node_id;
        this.payload = payload;
    }

    public DataTraffic(byte[] bytes) throws IOException {
        ByteArrayInputStream bArrayInputStream = new ByteArrayInputStream(bytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bArrayInputStream));

        din.readInt(); // take message_type out of buffer
        this.node_id = din.readInt();
        this.payload = din.readInt();

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
        dout.writeInt(this.payload);

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

    public int get_payload() {
        return this.payload;
    }
}