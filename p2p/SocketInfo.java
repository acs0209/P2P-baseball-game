package p2p;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketInfo {

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public SocketInfo() {

    }

    public SocketInfo(Socket socket, BufferedReader reader, PrintWriter writer) {
        this.socket = socket;
        this.reader = reader;
        this.writer = writer;
    }

    public Socket getSocket() {
        return socket;
    }

    public BufferedReader getReader() {
        return reader;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public void close() throws IOException {
        reader.close();
        writer.close();
        socket.close();
    }
}
