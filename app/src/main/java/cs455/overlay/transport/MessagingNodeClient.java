package cs455.overlay.transport;

import cs455.overlay.wireformats.RegisterResponse;
import cs455.overlay.wireformats.TaskInitiate;
import cs455.overlay.node.MessagingNode;
import cs455.overlay.wireformats.ConnectionsDirective;
import cs455.overlay.wireformats.RegisterRequest;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MessagingNodeClient implements Runnable {
    String server_address;
    int server_port;
    String ip;
    MessagingNode node;
    int port;
    Socket socket_to_server;

    // flags that messagingnode blocks on
    boolean initialization_complete;
    boolean partner_info_captured;
    boolean task_initiate_gotted;

    public MessagingNodeClient(String my_ip, int my_port, String server_address, int server_port, MessagingNode node) {
        this.ip = my_ip;
        this.port = my_port;
        this.server_address = server_address;
        this.server_port = server_port;
        this.node = node;

        this.initialization_complete = false;
        this.partner_info_captured = false;
        this.task_initiate_gotted = false;
    
        this.socket_to_server = null;
    }

    public boolean get_initialization_complete() {
        return initialization_complete;
    }

    public boolean get_partner_info_caputured() {
        return partner_info_captured;
    }

    public boolean get_task_initiate_gotted() {
        return task_initiate_gotted;
    }

    public void initialize_connection_with_registry() {
        try {
            // Create request
            RegisterRequest register_request = new RegisterRequest(this.ip, this.port);
            byte[] marshalled_request = register_request.marshall();
            System.out.println("Data marshalled in MessagingNodeClient");

            // Send request
            DataOutputStream output_stream = new DataOutputStream(socket_to_server.getOutputStream());
            output_stream.writeInt(marshalled_request.length);
            output_stream.write(marshalled_request);
            System.out.println("Data written in MessagingNodeClient");

            // Get ID from response
            DataInputStream input_stream = new DataInputStream(socket_to_server.getInputStream());

            System.out.println("Waiting for a response from the server");
            Integer incoming_message_length = 0;

            // This blocks until a message arrives
            incoming_message_length = input_stream.readInt();
            System.out.println("Server response received");
            byte[] incoming_request = new byte[incoming_message_length];
            input_stream.readFully(incoming_request);
            RegisterResponse register_response = new RegisterResponse(incoming_request);
            System.out.println("Response info: " + register_response.get_info());

            // Check for valid status
            if (register_response.get_status() == 1) {
                int id = register_response.get_id();
                System.out.println(
                        "ID Received for MessagingNode with ip: " + this.ip + ", port: " + this.port + ": " + id);
                node.setID(id);
            } else {
                System.out
                        .println("Received FAILED response status in node with IP " + this.ip + ", port " + this.port);
            }

        } catch (IOException e) {
            System.out.println(e);
            System.out.println(
                    "Error trying to receive/send data in MessagingNode with ip " + this.ip + " , port " + this.port);
        }
    }

    public void capture_partner_info() {
        // this just waits for a response from the server
        try {
            // Get ID from response
            DataInputStream input_stream = new DataInputStream(socket_to_server.getInputStream());

            System.out.println("Waiting for partner to be received from the server");
            Integer incoming_message_length = 0;

            // This blocks until a message arrives
            incoming_message_length = input_stream.readInt();
            System.out.println("Server partner response received");
            byte[] incoming_request = new byte[incoming_message_length];
            input_stream.readFully(incoming_request);
            ConnectionsDirective connections_directive = new ConnectionsDirective(incoming_request);
            System.out.println("Partner node id: " + connections_directive.get_id());
            node.set_partner_ip(connections_directive.get_ip());
            node.set_partner_port(connections_directive.get_port());
        } catch (IOException e) {
            System.out.println(e);
            System.out.println(
                    "Error trying to receive/send data in MessagingNode with ip " + this.ip + " , port " + this.port);
        }
    }

    public void wait_for_task_initiate() {
        try {
            DataInputStream input_stream = new DataInputStream(socket_to_server.getInputStream());

            System.out.println("Waiting for partner to be received from the server");
            Integer incoming_message_length = 0;

            // This blocks until a message arrives
            incoming_message_length = input_stream.readInt();
            System.out.println("Task initiate received");
            byte[] incoming_request = new byte[incoming_message_length];
            input_stream.readFully(incoming_request);
            TaskInitiate task_initiate = new TaskInitiate(incoming_request);
            System.out.println("Num to send: " + task_initiate.number);
            node.num_messages_to_send_from_self = task_initiate.number;
            node.NUMBER_OF_NODES = task_initiate.expected_messages /  task_initiate.number;
        }
        catch (IOException e) {
            System.out.println("elkfhjwkgiherikgehkghekgjeh");
        }
    }

    public void send_task_complete(byte[] bytes) {
        try {
            DataOutputStream outputStream = new DataOutputStream(socket_to_server.getOutputStream());
            outputStream.writeInt(bytes.length);
            outputStream.write(bytes);
            outputStream.close();
        } catch(Exception e) {
            System.err.println(e);
        }
    }

    @Override
    public void run() {
        // connect do the Registry
        try {
            socket_to_server = new Socket(server_address, server_port);
            System.out.println("Connected to the Server");
        } catch (IOException e) {
            System.out.println(e);
            System.out.println(
                    "Error connecting to the server from MessagingNode with ip " + this.ip + " , port " + this.port);
            System.exit(1);
        }

        // we can assume we are connected
        initialize_connection_with_registry();
        this.initialization_complete = true;
        // now that we are initialized, we can wait for partner info
        capture_partner_info();
        this.partner_info_captured = true;
        // now we can 
        wait_for_task_initiate();
        this.task_initiate_gotted = true;

        // wait for this node to finish
        while(!node.finished.get());
    }

}
