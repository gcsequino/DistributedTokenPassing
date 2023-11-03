package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TaskComplete extends Event {
    public int message_type = Protocol.TASK_COMPLETE;
    public int node_id;
    public String node_ip;
    public int node_port;
    public int messages_sent;
    public long sent_sum;
    public int messages_recieved;
    public long recieved_sum;

    public TaskComplete(int node_id, String node_ip, int node_port, int messages_sent, long sent_sum, int messages_recieved, long recieved_sum) {
        this.node_id = node_id;
        this.node_ip = node_ip;
        this.node_port = node_port;
        this.messages_sent = messages_sent;
        this.sent_sum = sent_sum;
        this.messages_recieved = messages_recieved;
        this.recieved_sum = recieved_sum;
    }

    public TaskComplete(byte[] bytes) throws IOException {
        ByteArrayInputStream bArrayInputStream = new ByteArrayInputStream(bytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bArrayInputStream));

        din.readInt(); // take message_type out of stream
        this.node_id = din.readInt();
        this.node_ip = din.readUTF();
        this.node_port = din.readInt();
        this.messages_sent = din.readInt();
        this.sent_sum = din.readLong();
        this.messages_recieved = din.readInt();
        this.recieved_sum = din.readLong();

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
        dout.writeInt(this.messages_sent);
        dout.writeLong(this.sent_sum);
        dout.writeInt(this.messages_recieved);
        dout.writeLong(this.recieved_sum);

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

    public int get_messages_recieved() {
        return this.messages_recieved;
    }

    public long get_recieved_sum() {
        return this.recieved_sum;
    }

    public int get_messages_sent () {
        return this.messages_sent;
    }

    public long get_sent_sum() {
        return this.sent_sum;
    }
}