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
                output.println("사용자가 LOGIN 되었습니다.");
            } else if (tokens[0].equals("SEARCH")) {
                Map<String, Peer> onlinePeerList = napsterServer.online_users();
                String peers = getPeerList();
                if (onlinePeerList != null) {
                    output.println(peers);
                } else {
                    output.println("로그인을 해야합니다.");
                }
            } else if (tokens[0].equals("LOGOFF")) {
                napsterServer.logoff(userId);
                output.println("사용자가 LOGOFF 되었습니다.");
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

    public synchronized String getPeerList() {
        Map<String, Peer> onlinePeerList = napsterServer.online_users();
        String peers = "";
        if (onlinePeerList != null) {
            for (Peer peer : onlinePeerList.values()) {
                peers += "IP:" + peer.getIp() + ",Port:" + peer.getPort() + " ";
            }
        }
        return peers;
    }
}