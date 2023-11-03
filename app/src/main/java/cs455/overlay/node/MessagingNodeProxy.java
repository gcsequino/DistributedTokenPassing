package cs455.overlay.node;

import java.net.Socket;

public class MessagingNodeProxy{
    public enum Status{WAITING, RUNNING, DONE};
    private int ID;
    private int port;
    private String IP;
    private Status status;
    private Socket socket;
    private int messages_sent;
    private int messages_recieved;
    private long sent_sum;
    private long received_sum;

    public MessagingNodeProxy(int ID, int port, String IP, Socket socket, int messages_sent, int messages_recieved, long sent_sum, long received_sum) {
        this.ID = ID;
        this.port = port;
        this.IP = IP;
        this.status = Status.RUNNING;
        this.socket = socket;
        this.messages_sent = messages_sent;
        this.messages_recieved = messages_recieved;
        this.sent_sum = sent_sum;
        this.received_sum = received_sum;
    }

    public int getMessagesSent() {
        return this.messages_sent;
    }

    public void setMessagesSent(int messages_sent) {
        this.messages_sent = messages_sent;
    }

    public int getMessagesRecieved() {
        return this.messages_recieved;
    }

    public void setMessagesRecieved(int messages_recieved) {
        this.messages_recieved = messages_recieved;
    }

    public long getSentSum() {
        return this.sent_sum;
    }

    public void setSentSum(long sent_sum) {
        this.sent_sum = sent_sum;
    }

    public long getRecievedSum() {
        return this.received_sum;
    }

    public void setRecievedSum(long received_sum) {
        this.received_sum = received_sum;
    }

    @Override
    public String toString(){
        return "|" + this.ID + " " + this.IP + " " + this.port + " " + this.status + " " + socket + "|\n" +
               "| rec:" + this.messages_recieved + " sent:" + this.messages_sent + " sent_sum:" + this.sent_sum + " rec_sum:" + this.received_sum;
    }

    public String tableRow() {
        return String.format("|  %5d  %12s  %17d  %17d  %17d  %17d  |", this.ID, this.IP, this.messages_recieved, this.messages_sent, this.received_sum, this.sent_sum);
    }

    public Status getStatus() {
        return status;
    }

    public int getID() {
        return this.ID;
    }

    public int getPort() {
        return this.port;
    }

    public String getIP() {
        return this.IP;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public MessagingNodeProxy setStatus(Status status) {
        this.status = status;
        return this;
    }
}