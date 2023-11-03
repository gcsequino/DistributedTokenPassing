package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegisterResponse extends Event{
    public int message_type = Protocol.REGISTER_RESPONSE;
    public byte status_code;
    public int node_id;
    public String info;
    
    public RegisterResponse(byte status, int id, String info) {
        status_code = status;
        node_id = id;
        this.info = info;
    }

    public RegisterResponse(byte[] bytes) throws IOException {
        ByteArrayInputStream bArrayInputStream = new ByteArrayInputStream(bytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bArrayInputStream));

        din.readInt(); // take message_type out of stream
        this.status_code = din.readByte();
        this.node_id = din.readInt();
        this.info = din.readUTF();

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
        dout.writeByte(this.status_code);
        dout.writeInt(this.node_id);
        dout.writeUTF(this.info);

        // cleanup
        dout.flush();
        marshalledBytes = bArrayOutputStream.toByteArray();
        bArrayOutputStream.close();
        dout.close();
        return marshalledBytes;
    }

    @Override
    public int get_message_type() {
        return this.get_message_type();
    }

    public byte get_status() {
        return this.status_code;
    }

    public int get_id() {
        return this.node_id;
    }

    public String get_info() {
        return this.info;
    }
}
