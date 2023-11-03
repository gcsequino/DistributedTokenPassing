package cs455.overlay.transport;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import cs455.overlay.node.MessagingNode;
import cs455.overlay.wireformats.DataTraffic;

public class MessagingNodeSender implements Runnable {
    String my_ip;
    int my_port;
    String partner_ip;
    int partner_port;
    ConcurrentLinkedQueue<DataTraffic> clq;
    MessagingNode node;

    public MessagingNodeSender(String my_ip, int my_port, String partner_ip, int partner_port, ConcurrentLinkedQueue<DataTraffic> clq, MessagingNode node) {
        this.my_ip = my_ip;
        this.my_port = my_port;
        this.partner_ip = partner_ip;
        this.partner_port = partner_port;
        this.clq = clq;
        this.node = node;
    }

    @Override
    public void run() {
        // sender thread
        System.out.println("i am in the sender thread and will be connecting to " + partner_ip + ":" + partner_port);
        try{
            Socket socketToTheReceiver = new Socket(partner_ip, partner_port);
            //Create output byte stream, send data
			DataOutputStream outputStream = new DataOutputStream(socketToTheReceiver.getOutputStream());
            while(node.num_messages_sent < node.num_messages_to_send_from_self * node.NUMBER_OF_NODES){
                // grab from queue
                DataTraffic traffic_to_send = clq.poll();
                if (traffic_to_send == null){
                    continue;
                }

                // add to sums
                node.num_messages_sent++;
                // node.cumsum_sent += traffic_to_send.payload;

                // send er' off
                byte[] outgoing_traffic = traffic_to_send.marshall();
                int outgoing_traffic_length = outgoing_traffic.length;
                outputStream.writeInt(outgoing_traffic_length);
                outputStream.write(outgoing_traffic, 0, outgoing_traffic_length);

                // nice to have log
                if (node.num_messages_sent % 50000 == 0) {
                    System.out.println("Sent " + node.num_messages_sent + " so far.");
                }
            }
            socketToTheReceiver.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
