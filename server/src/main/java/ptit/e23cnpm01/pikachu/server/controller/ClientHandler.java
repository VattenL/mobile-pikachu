package ptit.e23cnpm01.pikachu.server.controller;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import ptit.e23cnpm01.pikachu.core.model.Player;
import ptit.e23cnpm01.pikachu.core.protocol.Message;

/**
 * Một luồng cho mỗi client: đọc từng dòng JSON và chuyển cho lớp điều khiển theo type.
 *
 * <p>Phụ trách: Dev 2.
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private Player player;
    private BufferedReader reader;
    private PrintWriter writer;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        // TODO
    }

    public void send(Message msg) {
        // TODO
    }
}
