package p2p;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private int portNumber;
    private NapsterClient napsterClient;

    public ClientHandler() {

    }

    public ClientHandler(int portNumber, NapsterClient napsterClient) {
        this.portNumber = portNumber;
        this.napsterClient = napsterClient;
    }

    @Override
    public void run() {
        Socket clientSocket = null;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(portNumber);
            while (true) {

                clientSocket = serverSocket.accept();

                // 다른 peer가 접속할때마다 새로운 스레드 생성
                ReceiveThread receiveThread = new ReceiveThread(clientSocket, new ClientHandler());
                Thread t = new Thread(receiveThread);
                t.start();
            }
        } catch (Exception e) {
            System.out.println("유저와의 연결에 오류가 발생했습니다.");
        } finally {
            try {
                clientSocket.close();
                serverSocket.close();
            } catch (Exception e) {
                System.out.println("유저와의 연결에 오류가 발생했습니다.");
            }
        }
    }
}

class ReceiveThread implements Runnable {

    private final String answer = "123";
    private Socket gameSocket;
    private ClientHandler clientHandler;

    public ReceiveThread (Socket gameSocket, ClientHandler clientHandler) {
        this.gameSocket = gameSocket;
        this.clientHandler = clientHandler;
    }

    @Override
    public void run() {
        BufferedReader gameInput = null;
        PrintWriter gameOutput = null;
        try {
            gameInput = new BufferedReader(new InputStreamReader(gameSocket.getInputStream()));
            gameOutput = new PrintWriter(gameSocket.getOutputStream(), true);

            while (true) {
                String request = gameInput.readLine();
                String[] tokens = request.split(" ");
                String command = tokens[0];

                if (command.equals("guess") && tokens.length == 2) {
                    String inputNum = tokens[1];
                    String result = baseballGame(inputNum);
                    gameOutput.println(result);
                    if (result.equals("정답입니다.")) {
                        break;
                    }
                } else if (command.equals("disconnect")) {
                    System.out.println("상대방이 연결을 종료해 게임을 종료합니다.");
                    gameOutput.println("게임을 종료합니다.");
                    break;
                } else {
                    gameOutput.println("잘못된 입력입니다.");
                }
            }
        } catch (Exception e) {
            System.out.println("게임 중 오류가 발생했습니다.");
        } finally {
            try {
                if (gameInput != null) {
                    gameInput.close();
                }
                if (gameOutput != null) {
                    gameOutput.close();
                }
                if (gameSocket != null) {
                    gameSocket.close();
                }
            } catch (Exception e) {
                System.out.println("게임 중 오류가 발생했습니다.");
            }
        }
    }

    public synchronized String baseballGame(String guessNum) {
        int strike = 0;
        int ball = 0;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (guessNum.charAt(i) == answer.charAt(j)) {
                    ball += 1;
                    break;
                }
            }
            if (guessNum.charAt(i) == answer.charAt(i)) {
                strike += 1;
                ball -= 1;
            }
        }
        if (strike == 3) {
            return "정답입니다.";
        }
        String result = String.format("strike: %d, ball: %d", strike, ball);
        return result;
    }
}