package p2p;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

public class ServerHandler implements Runnable {

    private Socket clientSocket;
    private NapsterServer napsterServer;

    public ServerHandler(Socket clientSocket, NapsterServer napsterServer) {

        this.clientSocket = clientSocket;
        this.napsterServer = napsterServer;
    }

    @Override
    public void run() {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);

            String request = input.readLine();
            String[] tokens = request.split(" ");
            String userId = tokens[1];
            String ipAddress = tokens[2];
            String portNumber = tokens[3];

            if (tokens[0].equals("LOGIN")) {
                napsterServer.registerPeer(userId, ipAddress, Integer.parseInt(portNumber));
                output.println("LOGIN 하셨습니다.");
            } else if (tokens[0].equals("SEARCH")) {
                Map<String, Peer> onlinePeerList = napsterServer.online_users();
                String peers = getPeerList(userId);
                if (onlinePeerList != null) {
                    output.println(peers);
                } else {
                    output.println("현재 온라인에 아무도 존재하지 않습니다.");
                }
            } else if (tokens[0].equals("LOGOFF")) {
                napsterServer.logoff(userId);
                output.println("logoff 하셨습니다. 게임을 다시 하실려면 login 하셔야 합니다.");
            }
            else {
                output.println("Invalid request.");
            }

            input.close();
            output.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized String getPeerList(String userId) {
        Map<String, Peer> onlinePeerList = napsterServer.online_users();
        String peers = "";
        if (onlinePeerList != null) {
            for (String peer : onlinePeerList.keySet()) {
                if (!peer.equals(userId)) {
                    Peer peerInfo = onlinePeerList.get(peer);
                    peers += " IP: " + peerInfo.getIp() + " " + "Port: " + peerInfo.getPort() + " " + "userId: " + peer;
                }
            }
        }
        return peers;
    }
}