package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class EventFactory implements Protocol {
    public static Event newEvent(byte[] bytes) throws IOException {

        ByteArrayInputStream bArrayInputStream = new ByteArrayInputStream(bytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bArrayInputStream));

        int message_type = din.readInt(); // take message_type out of stream

        switch(message_type) {
            case REGISTER_REQUEST :
                return new RegisterRequest(bytes);
            case REGISTER_RESPONSE :
                return new RegisterResponse(bytes);
            case DERGEDISTER_REQUEST :
                return new Deregister(bytes);
            case CONNECTIONS_DIRECTIVE :
                return new ConnectionsDirective(bytes);
            case TASK_INITIATE :
                return new TaskInitiate(bytes);
            case DATA_TRAFFIC :
                return new DataTraffic(bytes);
            case TASK_COMPLETE :
                return new TaskComplete(bytes);
        }
        
        bArrayInputStream.close();
        din.close();

        return null;
    }
}