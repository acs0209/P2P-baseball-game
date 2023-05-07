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

public class NapsterClient {

    // 야구 게임을 하기위해 연결되어 있는 유저 목록
    private static Map<String, SocketInfo> gamePeers = new HashMap<>();
    private static Socket clientSocket;
    private static final String serverIpAddress = "localhost";
    private static final int serverPortNumber = 9999;
    private static final String clientIpAddress = "217.1.1.1";
    private static final int clientPortNumber = 4000;
    private static ClientHandler clientHandler;
    private static boolean loginCheck = false;
    private static final String serverCombined = clientIpAddress + ":" + clientPortNumber;
    private static final String serverUserId = sha256(serverCombined);

    public static void main(String[] args) throws IOException {

        serverConnect("LOGIN", serverUserId, serverIpAddress, serverPortNumber);
        clientHandler = new ClientHandler(4000, new NapsterClient());
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
            String userId = "";
            int portNumber = 0;
            if (tokens.length == 3) {
                ipAddress = tokens[1];
                portNumber = Integer.parseInt(tokens[2]);
                String combined = ipAddress + ":" + portNumber;
                userId = sha256(combined);
            }

            if (loginCheck == false && !command.equals("login")) {
                System.out.println("로그인을 하셔야 합니다.");
                continue;
            }

            if (command.equals("help")) {
                help();
            } else if (command.equals("online_users")) {
                online_users();
            } else if (command.equals("connect")) {
                connect(ipAddress, portNumber);
            } else if (command.equals("disconnect")) {
                disconnect(userId);
            } else if (command.equals("guess")) {
                guess(userId);
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
        System.out.println("disconnect [ip] [port]: end your game session with the given IP and port");
        System.out.println("guess [ip] [port]: announce that you are starting a game with peer that you've already initiated a game with via the \"connect\" command ");
        System.out.println("guess [your guessing number]: send a guessing number to the peer that you've already announced the start of the game with via the \"guess\" command ");
        System.out.println("logoff: send a message (notification) to register server for logging off");
    }

    public static void online_users() {
        serverConnect("SEARCH", serverUserId, serverIpAddress, serverPortNumber);
    }

    public static void connect(String ipAddress, int portNumber) {
        try {
            String combined = ipAddress + ":" + portNumber;
            String userId = sha256(combined);

            Socket gameSocket = new Socket(ipAddress, portNumber);
            BufferedReader gameInput = new BufferedReader(new InputStreamReader(gameSocket.getInputStream()));
            PrintWriter gameOutput = new PrintWriter(gameSocket.getOutputStream(), true);
            SocketInfo socketInfo = new SocketInfo(gameSocket, gameInput, gameOutput);
            gamePeers.put(userId, socketInfo);
            System.out.println("다른 유저와 연결되었습니다.");
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
            System.out.println("연결이 해제 되었습니다.");
        } catch (Exception e) {
            System.out.println("연결 해재에 실패했습니다.");
        }
    }

    public static void guess(String userId) {
        SocketInfo socketInfo = gamePeers.get(userId);
        BufferedReader input = null;
        PrintWriter output = null;
        Socket gameSocket = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.println("요청하신 유저와의 숫자야구 게임을 시작합니다.");
            gameSocket = socketInfo.getSocket();
            input = socketInfo.getReader();
            output = socketInfo.getWriter();
            while (true) {
                System.out.print("예상 번호를 입력하세요: ");
                String inputNum = br.readLine();
                output.println(inputNum);
                String response = input.readLine();
                System.out.println(response);
                if (response.equals("정답입니다.") || response.equals("게임을 종료합니다.")) {
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("게임 중 오류가 발생했습니다.");
        } finally {
            try {
                input.close();
                output.close();
                gameSocket.close();
            } catch (Exception e) {
                System.out.println("게임 중 오류가 발생했습니다.");
            }
        }
    }

    public static void logoff() {
        for (String key : gamePeers.keySet()) {
            disconnect(key);
        }
        System.out.println("logoff 하셨습니다. 게임을 다시 하실려면 login을 하셔야 합니다.");
        serverConnect("LOGOFF", serverUserId, serverIpAddress, serverPortNumber);
    }
    public static void login() {
        System.out.println("login 하셨습니다.");
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