package ptit.e23cnpm01.pikachu.network;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import ptit.e23cnpm01.pikachu.core.protocol.Message;

/**
 * Kết nối tới server (một đối tượng duy nhất): đọc ghi trên luồng nền, gọi MessageListener trên luồng giao diện.
 *
 * <p>Phụ trách: Dev 2.
 */
public class ServerConnection {
    private Socket socket;
    private ArrayList<MessageListener> listeners;
    private BufferedReader reader;
    private PrintWriter writer;
    /** Gửi PING mỗi 5 giây. */
    private Timer pingTimer;

    public static ServerConnection getInstance() {
        throw new UnsupportedOperationException("TODO");
    }

    public void send(Message msg) {
        // TODO
    }

    public void addListener(MessageListener l) {
        // TODO
    }

    public void removeListener(MessageListener l) {
        // TODO
    }

    public boolean connect(String host, int port) {
        throw new UnsupportedOperationException("TODO");
    }
}
