package cs455.overlay.node;

import cs455.overlay.transport.*;
import cs455.overlay.wireformats.DataTraffic;
import cs455.overlay.wireformats.TaskComplete;

import java.io.IOException;
import java.net.*;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessagingNode implements Node {
    //my port and ip
    int my_port;
    String my_ip;

    // parterns port and ip (set after getting from registry)
    int partner_port;
    String partner_ip;

    // registry port and ip
    int registry_port;
    String registry_ip;

    // all of the state
    public int NUMBER_OF_NODES;
    public int num_messages_to_send_from_self;
    public int num_messages_sent;
    public int num_messages_received;
    public long cumsum_received;
    public long cumsum_sent;
    public AtomicBoolean finished;


    ServerSocket server_socket;
    ConcurrentLinkedQueue<DataTraffic> clq;
    public int ID = 0;
    MessagingNodeClient node_client;

    public MessagingNode(String my_ip, String registry_ip, int registry_port) {
        clq = new ConcurrentLinkedQueue<DataTraffic>();
        this.my_ip = my_ip;
        this.registry_ip = registry_ip;
        this.registry_port = registry_port;

        Random random = new Random();

        //get my random port from 1025 to 61025, retry if it isnt available
        do {
            this.my_port = 1025 + random.nextInt(60000);
        } while(!available(my_port));
        System.out.println("messaging node's random port is " + my_port);

        this.cumsum_received = 0;
        this.cumsum_sent = 0;
        this.num_messages_sent = 0;
        this.num_messages_received = 0;
        this.finished = new AtomicBoolean();
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public static boolean ip_is_valid(String ip) {
        try {
            Inet4Address.getByName(ip);
        } catch(Exception e) {return false;}
        return true;
    }


    private boolean available(int port) {
        try (Socket ignored = new Socket("localhost", port)) {
            return false;
        } catch (IOException ignored) {
            return true;
        }
    }

    // public static String[] messaging_startup_cli(String[] args) {
    //     if(args.length != 2) {
    //         System.out.printf("Invald usage! Expected: runMessagingNode <ip_address>\n");
    //         return null;
    //     }
    //     String ip = args[0];
    //     if(!ip_is_valid(ip)) {
    //         System.out.printf("Invald IP!");
    //         return null;
    //     }

    //     String[] ret = {ip, port+""};
    //     return ret;
    // }


    public void start_sender_thread() {
        System.out.println("i am starting sender thread");
        // send stuff
        MessagingNodeSender sender = new MessagingNodeSender(my_ip, my_port, partner_ip, partner_port, clq, this);
        Thread thread = new Thread(sender);
        thread.start();
    }

    public void start_receiver_thread() {
        System.out.println("i am starting the receiver thread");
        MessagingNodeReceiver receiver = new MessagingNodeReceiver(my_port, clq, this);
        Thread thread = new Thread(receiver);
        thread.start();
    }

    public void start_registry_client_thread() {
        System.out.println("Starting Registry Client thread in Messaging Node");
        node_client = new MessagingNodeClient(my_ip, my_port, registry_ip, registry_port,  this);
        Thread thread = new Thread(node_client);
        thread.start();

        // block until response has been gotten
        while(!node_client.get_initialization_complete()) {
            try {
                // this here cause it dont work without this
                Thread.sleep(1);
            } catch (Exception e) {}
        }
        System.out.println("Initialization with registry is complete!");
    }

    public void wait_for_partner_info() {
        // block until partner info has been gotted
        while(!node_client.get_partner_info_caputured()){
            try{
                // this here cause it dont work without this
                Thread.sleep(1);
            } catch (Exception e) {}
        }
        System.out.println("Got partner info!!!");
    }

    public void wait_for_task_initiate() {
        // block until partner info has been gotted
        while(!node_client.get_task_initiate_gotted()){
            try{
                // this here cause it dont work without this
                Thread.sleep(1);
            } catch (Exception e) {}
        }
        System.out.println("Got task initiate!!!");
    }

    public void set_partner_ip(String partner_ip) {
        this.partner_ip = partner_ip;
    }

    public void set_partner_port(int partner_port) {
        this.partner_port = partner_port;
    }

    // puts the inital `num_messages_to_send` into the clq to send off
    public void load_em_up(){ 
        Random rand = new Random();
        for(int i = 0; i < num_messages_to_send_from_self; i++) {
            boolean pos_or_neg = rand.nextBoolean();
            int random_value = pos_or_neg ? rand.nextInt(2147483647) : rand.nextInt(2147483647) - 2147483647 - 1;
            this.cumsum_sent += random_value;
            clq.add(new DataTraffic(this.ID, random_value));
            // System.out.println(clq.size());
        }
    }

    public void poll_for_done() throws Exception {
        //wait for both sender and receiver to be done
        while(num_messages_sent < num_messages_to_send_from_self * NUMBER_OF_NODES || num_messages_received < num_messages_to_send_from_self * NUMBER_OF_NODES) {
            Thread.sleep(100);
        }
        //done!
        System.out.println("\n\n=-=-=-=-=-=-=-=-=\n");
        System.out.println("Sent " + num_messages_sent + " total. Cumsum sent is " + cumsum_sent);
        System.out.println("Received " + num_messages_received + " total. Cumsum received is " + cumsum_received);
    }

    public void task_complete() {
        TaskComplete payload = new TaskComplete(this.ID, this.my_ip, this.my_port, this.num_messages_sent, this.cumsum_sent, this.num_messages_received, this.cumsum_received);
        byte[] bytes = null;
        try {
            bytes = payload.marshall();
        } catch(Exception e) {
            System.err.println(e);
            System.err.printf("Failed to marshall TaskComplete on node %d\n", this.ID);
        }
        node_client.send_task_complete(bytes);
        finished.set(true);
    }

    public static void main(String[] args) {
        // gather startup info
        String my_ip = args[0];
        String registry_ip = args[1];
        int registry_port = Integer.parseInt(args[2]);

        // start node and threads
        MessagingNode this_node = new MessagingNode(my_ip, registry_ip, registry_port);

        // This will block until connected to server
        this_node.start_registry_client_thread();


        // This will block until partner info has been gotted
        this_node.wait_for_partner_info();

        this_node.start_receiver_thread();

        //sleep so receiver is ready on all clients
        this_node.wait_for_task_initiate();

        // This is where the magic happens

        this_node.start_sender_thread();
        
        // load initial values into clq
        this_node.load_em_up();

        // poll for being done
        try {
            this_node.poll_for_done();
        }
        catch(Exception e){}

        // send TaskComplete
        this_node.task_complete();
    }
}
