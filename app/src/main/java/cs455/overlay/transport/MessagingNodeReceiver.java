package cs455.overlay.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import cs455.overlay.node.MessagingNode;
import cs455.overlay.wireformats.DataTraffic;

public class MessagingNodeReceiver implements Runnable {
    int my_port;
    ConcurrentLinkedQueue<DataTraffic> clq;

    // just a way to generate a non-random payload that can be differentiated
    int current_count;

    MessagingNode node;

    public MessagingNodeReceiver(int my_port, ConcurrentLinkedQueue<DataTraffic> clq, MessagingNode node) {
        this.my_port = my_port;
        this.clq = clq;
        this.current_count = 0;
        this.node = node;
    }

    @Override
    public void run() {
        try {
            System.out.println("i am in receiver thread");

            ServerSocket server_socket = new ServerSocket(this.my_port);
            Socket receiver_socket = server_socket.accept();
            DataInputStream input_stream = new DataInputStream(receiver_socket.getInputStream());
            int dataLength;
            while (receiver_socket != null && node.num_messages_received < node.num_messages_to_send_from_self * node.NUMBER_OF_NODES) {
                try {
                    // get the data from socket
                    dataLength = input_stream.readInt();
                    byte[] incoming_data = new byte[dataLength];
                    input_stream.readFully(incoming_data, 0, dataLength);

                    // we have data, turn into DataTraffic
                    DataTraffic recvd_traffic = new DataTraffic(incoming_data);

                    //add to sums
                    node.cumsum_received += recvd_traffic.payload;
                    node.num_messages_received++;

                    // nice to have this log
                    if (node.num_messages_received % 50000 == 0) {
                        System.out.println("Received " + node.num_messages_received + " so far");
                    }

                    // add it to the queue only if it is not originally from this node
                    if(recvd_traffic.node_id != node.ID) {
                        clq.add(recvd_traffic);
                    }
                } catch (SocketException se) {
                    System.out.println(se.getMessage());
                    break;
                } catch (IOException ioe) {
                    System.out.println(ioe.getMessage());
                    break;
                }
                server_socket.close();
            }
        } 
        catch (IOException e) {
            // Sorry for the descriptive error message, Daniel
            System.out.println("IOException caught in Receiver.java");
            System.out.println(e);
        }
    }
}
