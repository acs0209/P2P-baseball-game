package p2p;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

// Napster Server
public class NapsterServer {

    private static Map<String, Peer> onlinePeerMap = new HashMap<>();
    private static final int serverPortNum = 9999;

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPortNum);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread t = new Thread(new ServerHandler(clientSocket, new NapsterServer()));
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void registerPeer(String userId, String ipAddress, int portNumber) {
        Peer peer = new Peer(ipAddress, portNumber);
        onlinePeerMap.put(userId, peer);
    }

    public static synchronized Map<String, Peer> online_users() {
        return onlinePeerMap;
    }

    public static synchronized void logoff(String userId) {
        onlinePeerMap.remove(userId);
    }
}