package cs455.overlay.wireformats;

public interface Protocol {
    // message types
    public static final int REGISTER_REQUEST = 1;
    public static final int REGISTER_RESPONSE = 2;
    public static final int DERGEDISTER_REQUEST = 3;
    public static final int CONNECTIONS_DIRECTIVE = 4;
    public static final int TASK_INITIATE = 5;
    public static final int DATA_TRAFFIC = 6;
    public static final int TASK_COMPLETE = 7;
    public static final int PULL_TRAFFIC_SUMMARY = 8;
    public static final int TRAFFIC_SUMMARY = 9;
    // status codes
    public static final byte SUCCESS = 0;
    public static final byte FAILURE = 1;
}