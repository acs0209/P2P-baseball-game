package p2p;

public class Peer {

    private String ip;
    private int port;

    public Peer(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}