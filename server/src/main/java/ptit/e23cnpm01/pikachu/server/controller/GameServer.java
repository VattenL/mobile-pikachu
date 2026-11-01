package ptit.e23cnpm01.pikachu.server.controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

/**
 * Lớp chạy của server: mở ServerSocket, mỗi client một ClientHandler.
 *
 * <p>Phụ trách: Dev 2.
 */
public class GameServer {
    private static ServerSocket serverSocket;

    /** Đọc cổng từ server.properties, mở ServerSocket rồi lặp chờ kết nối; mỗi client một ClientHandler trên luồng mới. */
    public static void main(String[] args) throws IOException {
        Properties p = ServerConfig.load();
        int port = Integer.parseInt(p.getProperty("port", "9999").trim());
        serverSocket = new ServerSocket(port);
        System.out.println("Server Pikachu đang chạy trên cổng " + port + ", chờ client kết nối...");
        while (true) {
            Socket socket = serverSocket.accept();
            ClientHandler handler = new ClientHandler(socket);
            new Thread(handler, "client-" + socket.getRemoteSocketAddress()).start();
        }
    }
}
