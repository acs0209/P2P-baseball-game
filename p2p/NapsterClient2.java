package p2p;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class NapsterClient2  {

    // 야구 게임을 하기위해 연결되어 있는 유저 목록
    private static Map<String, SocketInfo> gamePeers = new HashMap<>();
    // 서버와 연결되는 소켓
    private static Socket clientSocket;
    private static final String serverIpAddress = "localhost";
    private static final int serverPortNumber = 9999;
    private static final String clientIpAddress = "localhost";
    private static final int clientPortNumber = 5000;
    private static boolean loginCheck = false;
    private static final String serverCombined = clientIpAddress + ":" + clientPortNumber;
    private static final String serverUserId = sha256(serverCombined);

    public static void main(String[] args) throws IOException {

        serverConnect("LOGIN", serverUserId, serverIpAddress, serverPortNumber);
        ClientHandler2 clientHandler = new ClientHandler2(5000, new NapsterClient2());
        Thread thread = new Thread(clientHandler);
        thread.start();

        while (true) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("입력: ");
            String input = br.readLine();
            String[] tokens = input.split(" ");
            String command = tokens[0];

            // 연결할 peer 의 ip 주소 와 port 번호
            String ipAddress = "";
            int portNumber = 0;
            String userId = "";

            if (loginCheck == false && !command.equals("login")) {
                System.out.println("로그인을 하셔야 합니다.");
                continue;
            }

            if (command.equals("help")) {
                help();
            } else if (command.equals("online_users")) {
                online_users();
            } else if (command.equals("connect") && tokens.length == 3) {
                ipAddress = tokens[1];
                portNumber = Integer.parseInt(tokens[2]);
                connect(ipAddress, portNumber);
            } else if (command.equals("disconnect") && tokens.length == 2) {
                userId = tokens[1];
                disconnect(userId);
            } else if (command.equals("guess") && tokens.length == 3) {
                userId = tokens[1];
                String guessNumber = tokens[2];
                guess(userId, guessNumber);
            } else if (command.equals("logoff")) {
                logoff();
            } else if (command.equals("login")) {
                login();
            } else {
                System.out.println("Invalid request.");
            }
        }
    }

    public static void serverConnect(String command, String userId, String ipAddress, int portNumber) {
        BufferedReader input = null;
        PrintWriter output = null;

        try {
            clientSocket = new Socket(ipAddress, portNumber);
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);
            output.println(command + " " + userId + " " + clientIpAddress + " " + clientPortNumber);
            String response = input.readLine();
            System.out.println(response);
        } catch (Exception e) {
            System.out.println("서버와 연결중 오류가 발생했습니다.");
        } finally {
            try {
                input.close();
                output.close();
                clientSocket.close();
            } catch (Exception e) {
                System.out.println("서버와 연결중 오류가 발생했습니다.");
            }
        }

        if (command.equals("LOGIN")) {
            loginCheck = true;
        } else if (command.equals("LOGOFF")) {
            loginCheck = false;
        }
    }

    public static void help() {
        System.out.println("help: lookup command (display all possible commands and their description)");
        System.out.println("online_users: send a request to the register server, get back a list of all online peers and display them on the screen");
        System.out.println("connect [ip] [port]: request to play a game with the given IP and port");
        System.out.println("disconnect [peer] : end your game session with the listed peer");
        System.out.println("guess [peer] [your guessing number] : send a guessing number to the peer that you've already initiated a game with via the \"connect\" command");
        System.out.println("logoff: send a message (notification) to register server for logging off");
        System.out.println("login: send a message (notification) to register server for login on");
    }

    public static void online_users() {
        serverConnect("SEARCH", serverUserId, serverIpAddress, serverPortNumber);
    }

    public static void connect(String ipAddress, int portNumber) {
        try {
            System.out.println("요청하신 유저와의 숫자야구 게임을 시작합니다.");
            String combined = ipAddress + ":" + portNumber;
            String userId = sha256(combined);
            Socket gameSocket = new Socket(ipAddress, portNumber);
            BufferedReader gameInput = new BufferedReader(new InputStreamReader(gameSocket.getInputStream()));
            PrintWriter gameOutput = new PrintWriter(gameSocket.getOutputStream(), true);
            SocketInfo socketInfo = new SocketInfo(gameSocket, gameInput, gameOutput);
            gamePeers.put(userId, socketInfo);
        } catch (Exception e) {
            System.out.println("유저와의 연결에 실패했습니다.");
        }
    }

    public static void disconnect(String userId) {
        SocketInfo socketInfo = new SocketInfo();
        try {
            socketInfo = gamePeers.get(userId);
            PrintWriter output = socketInfo.getWriter();
            output.println("disconnect");
            socketInfo.close();
            System.out.println("게임이 종료됩니다.");
        } catch (Exception e) {
            System.out.println("연결 해재에 실패했습니다.");
        }
    }

    public static void guess(String userId, String guessNumber) {
        SocketInfo socketInfo = gamePeers.get(userId);
        BufferedReader input = null;
        PrintWriter output = null;
        Socket gameSocket = null;
        try {
            gameSocket = socketInfo.getSocket();
            input = socketInfo.getReader();
            output = socketInfo.getWriter();
            String guess = "guess" + " " + userId + " " + guessNumber;
            output.println(guess);
            String response = input.readLine();
            System.out.println(response);

        } catch (Exception e) {
            System.out.println("게임 중 오류가 발생했습니다.");
        }
    }

    public static void logoff() {
        for (String key : gamePeers.keySet()) {
            disconnect(key);
        }
        serverConnect("LOGOFF", serverUserId, serverIpAddress, serverPortNumber);
    }
    public static void login() {
        serverConnect("LOGIN", serverUserId, serverIpAddress, serverPortNumber);
    }

    private static String sha256(String str) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(str.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}