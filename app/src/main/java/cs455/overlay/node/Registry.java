package cs455.overlay.node;

import cs455.overlay.node.MessagingNodeProxy.Status;
import cs455.overlay.transport.RegistrySocket;

import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;
import java.math.BigInteger;
import java.net.*;

public class Registry implements Node{
    ConcurrentHashMap<Integer, MessagingNodeProxy> overlay;
    int registry_server_port;
    ServerSocket registry_socket = null;
    int max_nodes;

    int total_messages_sent = 0;
    int total_messages_received = 0;
    long sum_messages_sent = 0;
    long sum_messages_received = 0;

    public Registry(int registry_server_port, int max_nodes){
        this.registry_server_port = registry_server_port;
        this.max_nodes = max_nodes;
        overlay = new ConcurrentHashMap<Integer, MessagingNodeProxy>();
    }

    public void createOverlay() {
        try {
            registry_socket = new ServerSocket(registry_server_port, max_nodes);
            System.out.println("Waiting for connections...");
        } catch(IOException e) {
            System.out.println("Error creating Registry ServerSocket");
            System.exit(1);
        }

        // Wait for overlay to fill
        int connections = 0;
        while(connections < max_nodes) {
            try {
                // Wait for connections
                Socket incoming_connection = registry_socket.accept();
                connections++;

                // Create & start thread for new socket - registry/node communication
                RegistrySocket socket = new RegistrySocket(overlay, incoming_connection, max_nodes);
                Thread socket_thread = new Thread(socket);
                socket_thread.start();
            } catch (IOException e) {
                System.out.println("Error creating RegistrySocket");
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    @Override
    public String toString() {
        return  "---Overlay---\n" + this.overlay.toString();
    }

    public void updateStats() {
        for(int i : overlay.keySet()) {
            MessagingNodeProxy n = overlay.get(i);
            total_messages_sent += n.getMessagesSent();
            total_messages_received += n.getMessagesRecieved();
            sum_messages_sent += n.getSentSum();
            sum_messages_received += n.getRecievedSum();
        }
    }

    public void printRegistryTable() {
        updateStats();
        String data = "\nTotal Messages Sent: " + total_messages_sent + 
        "\nTotal Messages Received: " + total_messages_received + 
        "\nSum of Messages Sent: " + sum_messages_sent + 
        "\nSum of Messages Sent * Number of Nodes: " + sum_messages_sent * max_nodes +
        "\nSum of Messages Received: " + sum_messages_received;
        System.out.println(data);
    }

    public static void check_args(String[] args) {
        if(args.length != 2) {
            System.err.println("Invalid usage, expected <port> <max_nodes>");
            System.exit(1);
        }
    }

    private void printOverlay() {
        System.out.println("*** Table ***");
        System.out.println(String.format("|  %5s  %12s  %17s  %17s  %17s  %17s  |", "ID", "IP address", "messages_recieved", "messages_sent", "recieved_sum", "sent_sum"));
        for(MessagingNodeProxy current_node : overlay.values()) {
            System.out.println(current_node.tableRow());
        }
    }

    private void waitForMessagingNodes() {
        boolean running;
        do {
            running = false;
            for(int id : overlay.keySet()) {
                if(overlay.get(id).getStatus() == Status.RUNNING)
                    running = true;
            }
        } while(running);
    }

    public static void main(String[] args) {
        check_args(args);
        int my_port = Integer.parseInt(args[0]);
        int max_nodes = Integer.parseInt(args[1]);
        Registry registry = new Registry(my_port, max_nodes);
        registry.createOverlay();
        registry.overlay.toString();
        registry.waitForMessagingNodes();
        registry.printOverlay();
        registry.printRegistryTable();
    }
}
