package cs455.overlay.wireformats;

import java.io.IOException;

public abstract class Event{
    abstract public int get_message_type();
    abstract public byte[] marshall() throws IOException;
}