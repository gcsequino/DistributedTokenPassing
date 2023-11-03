package cs455.overlay.transport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import cs455.overlay.node.MessagingNodeProxy;
import cs455.overlay.node.MessagingNodeProxy.Status;
import cs455.overlay.wireformats.ConnectionsDirective;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;
import cs455.overlay.wireformats.Protocol;
import cs455.overlay.wireformats.RegisterRequest;
import cs455.overlay.wireformats.RegisterResponse;
import cs455.overlay.wireformats.TaskComplete;
import cs455.overlay.wireformats.TaskInitiate;

public class RegistrySocket implements Runnable {

    ConcurrentHashMap<Integer, MessagingNodeProxy> overlay;
    Random rng = new Random();
    Socket socket;
    int max_nodes;
    DataInputStream input_stream = null;
    DataOutputStream output_stream = null;
    int my_id;

    public RegistrySocket(ConcurrentHashMap<Integer, MessagingNodeProxy> overlay, Socket socket, int max_nodes) {
        this.overlay = overlay;
        this.socket = socket;
        this.max_nodes = max_nodes;
    }

    @Override
    public void run() {
        setupComminucation();
        registerNode();
        waitForOverlayToFill();
        sendConnectionDirective();
        try {
            // sleep for fun!
            Thread.sleep(1000);
        } catch (Exception e) {
            System.out.println("Uh oh!");
        }
        sendTaskInitiate();
        recieveTaskComplete();
    }

    private void updateOverlay(TaskComplete e) {
        int key = e.node_id;
        overlay.get(key).setStatus(Status.DONE);
        overlay.get(key).setMessagesRecieved(e.messages_recieved);
        overlay.get(key).setMessagesSent(e.messages_sent);
        overlay.get(key).setRecievedSum(e.recieved_sum);
        overlay.get(key).setSentSum(e.sent_sum);
    }

    private void recieveTaskComplete() {
        try {
            byte[] bytes = readInput();
            Event e = EventFactory.newEvent(bytes);
            if (e.get_message_type() != Protocol.TASK_COMPLETE) {
                System.err.println("Registry recieved unexpected event");
            }
            updateOverlay((TaskComplete) e);
        } catch (Exception e) {
            System.out.println("Error in recieveTaskComplete");
            System.out.println(e);
        }
    }

    private void setupComminucation() {
        try {
            input_stream = new DataInputStream(socket.getInputStream());
            output_stream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Error setting up input/output streams");
            e.printStackTrace();
        }
    }

    private void registerNode() {
        try {
            byte[] incoming_request = readInput();
            RegisterRequest register_request = new RegisterRequest(incoming_request);
            boolean request_is_valid = errorCheckRegisterRequest(register_request);
            RegisterResponse register_response = createRegisterResponse(request_is_valid, register_request);
            byte[] marshalled_response = register_response.marshall();
            writeOutput(marshalled_response);
        } catch (IOException e) {
            System.out.println("Error while RegistrySocket is accepting incoming socket requests");
            e.printStackTrace();
        }
    }

    private void sendConnectionDirective() {
        MessagingNodeProxy my_partner = findMyParter();
        int partner_id = my_partner.getID();
        String partner_ip = my_partner.getIP();
        int partner_port = my_partner.getPort();
        ConnectionsDirective connection_directive = new ConnectionsDirective(partner_id, partner_ip, partner_port);
        try {
            byte[] marshalled_connection_directive = connection_directive.marshall();
            writeOutput(marshalled_connection_directive);
        } catch (IOException e) {
            System.out.println("Error while trying to marshall/send connection directive in RegistrySocket");
            e.printStackTrace();
        }
    }

    private void sendTaskInitiate() {
        TaskInitiate task_initiate = new TaskInitiate(250000, 250000 * max_nodes);
        try {
            byte[] initiate_bytes =  task_initiate.marshall(); 
            writeOutput(initiate_bytes);
        }
        catch (IOException e) {
            System.out.println("Couldnt send task initiate cause it didnt work");
            e.printStackTrace();
        }

    }

    private MessagingNodeProxy findMyParter() {
        ArrayList<Integer> overlay_proxy = new ArrayList<Integer>(overlay.keySet());
        Collections.sort(overlay_proxy);
        int next_node_id;
        int my_index = overlay_proxy.indexOf(my_id);
        if (my_index != overlay_proxy.size() - 1) {
            next_node_id = overlay_proxy.get(my_index + 1);
        } else {
            next_node_id = overlay_proxy.get(0);
        }
        return overlay.get(next_node_id);
    }

    private void writeOutput(byte[] marshalled_data) {
        try {
            output_stream.writeInt(marshalled_data.length);
            output_stream.write(marshalled_data);
        } catch (IOException e) {
            System.out.println("Error writing output");
            e.printStackTrace();
        }
    }

    private byte[] readInput() {
        byte[] incoming_request;
        try {
            int message_length = input_stream.readInt();
            incoming_request = new byte[message_length];
            input_stream.readFully(incoming_request);
        } catch (IOException e) {
            incoming_request = null;
            System.out.println("Error reading message in RegistrySocket");
            e.printStackTrace();
        }
        return incoming_request;
    }

    private RegisterResponse createRegisterResponse(boolean request_is_valid, RegisterRequest register_request) {
        RegisterResponse register_response;
        if (request_is_valid) {
            int node_id = getUniqueID();
            my_id = node_id;
            MessagingNodeProxy node = new MessagingNodeProxy(node_id, register_request.get_port(),
                    register_request.get_ip(), socket, 0, 0, 0, 0);
            overlay.put(node_id, node);
            register_response = new RegisterResponse((byte) 1, node_id,
                    "Registration successful. Current number of nodes in overlay: " + overlay.size());
        } else {
            register_response = new RegisterResponse((byte) 0, 0, "Registration Failed.");
        }
        return register_response;
    }

    private int getUniqueID() {
        int node_id = rng.nextInt(1024);
        while (overlay.get(node_id) != null) {
            node_id = rng.nextInt(1024);
        }
        return node_id;
    }

    private boolean errorCheckRegisterRequest(RegisterRequest register_request) {
        for (MessagingNodeProxy node : overlay.values()) {
            if (node.getIP().equals(register_request.get_ip()) && node.getPort() == register_request.get_port()) {
                System.out.println("Error: Node already registered");
                return false;
            }
            // Error check IP
            // FIXME how do i get the address of a DataInputStream?
            // if(!register_request.get_ip().equals(input_stream.getHostAddress())) {
            // System.out.println("Error: Node inaccurately reported it's IP");
            // request_is_valid = false;
            // }
        }
        return true;
    }

    private void waitForOverlayToFill() {
        while (overlay.size() < max_nodes) {
        }
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
