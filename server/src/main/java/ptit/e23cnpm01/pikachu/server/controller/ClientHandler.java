package ptit.e23cnpm01.pikachu.server.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import ptit.e23cnpm01.pikachu.core.model.Player;
import ptit.e23cnpm01.pikachu.core.protocol.Message;
import ptit.e23cnpm01.pikachu.core.protocol.MessageType;

/**
 * Một luồng cho mỗi client: đọc từng dòng JSON và chuyển cho lớp điều khiển theo type.
 *
 * <p>Phụ trách: Dev 2.
 */
public class ClientHandler implements Runnable {
    /** Quá thời gian này không nhận được dòng nào (client gửi PING mỗi 5 giây) thì coi như mất kết nối. */
    private static final int READ_TIMEOUT_MS = 15000;

    private Socket socket;
    /** Người chơi đã đăng nhập trên kết nối này, rỗng nếu là khách. */
    private volatile Player player;
    /** Phòng chơi hiện tại (đang chơi hoặc ở dialog kết quả), rỗng nếu không có. */
    private volatile Integer roomId;
    private BufferedReader reader;
    private PrintWriter writer;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    /**
     * Đọc từng dòng, dựng Message và chuyển cho hàm xử lý theo type. Kết nối chưa đăng nhập (khách) chỉ được gửi
     * REGISTER, LOGIN, GET_RANKING, PING; message khác bị bỏ qua. Khi đọc lỗi, hết luồng hoặc quá 15 giây không có
     * dòng nào: xử lý mất kết nối rồi đóng socket.
     */
    @Override
    public void run() {
        try {
            socket.setSoTimeout(READ_TIMEOUT_MS);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            synchronized (this) {
                writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            }
            String line;
            while ((line = reader.readLine()) != null) {
                Message msg;
                try {
                    msg = Message.fromJson(line);
                } catch (IllegalArgumentException e) {
                    System.err.println(socket.getRemoteSocketAddress() + ": ignored invalid line: " + line);
                    continue;
                }
                String type = msg.getType();
                if (player == null && !type.equals(MessageType.REGISTER) && !type.equals(MessageType.LOGIN)
                        && !type.equals(MessageType.GET_RANKING) && !type.equals(MessageType.PING)) {
                    continue;
                }
                try {
                    switch (type) {
                        case MessageType.PING:
                            break;
                        case MessageType.REGISTER:
                            new LoginController().handleRegister(this, msg);
                            break;
                        case MessageType.LOGIN:
                            new LoginController().handleLogin(this, msg);
                            break;
                        case MessageType.LOGOUT:
                            new LoginController().handleLogout(this, msg);
                            break;
                        case MessageType.GET_RANKING:
                            new RankingController().handleGetRanking(this, msg);
                            break;
                        case MessageType.GET_ONLINE_LIST:
                            new ChallengeController().handleGetOnlineList(this, msg);
                            break;
                        case MessageType.UPDATE_STATUS:
                            new ChallengeController().handleUpdateStatus(this, msg);
                            break;
                        case MessageType.INVITE:
                            new ChallengeController().handleInvite(this, msg);
                            break;
                        case MessageType.INVITE_CANCEL:
                            new ChallengeController().handleInviteCancel(this, msg);
                            break;
                        case MessageType.INVITE_REPLY:
                            new ChallengeController().handleInviteReply(this, msg);
                            break;
                        case MessageType.MOVE:
                            new MatchController().handleMove(this, msg);
                            break;
                        case MessageType.LEAVE_MATCH:
                            new MatchController().handleLeaveMatch(this, msg);
                            break;
                        case MessageType.REMATCH_REPLY:
                            new MatchController().handleRematchReply(this, msg);
                            break;
                        default:
                            System.err.println(socket.getRemoteSocketAddress() + ": ignored message of type " + type);
                    }
                } catch (RuntimeException e) {
                    // lỗi khi xử lý một message không làm đứt kết nối của client
                    System.err.println(socket.getRemoteSocketAddress() + ": error while handling " + line);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            // lỗi đọc hoặc quá 15 giây không có dòng nào (SocketTimeoutException): mất kết nối
        } finally {
            try {
                if (roomId != null) {
                    new MatchController().handleDisconnect(this);
                }
                if (player != null && OnlinePlayerManager.getInstance().removeOnlinePlayer(this)) {
                    ChallengeController challengeController = new ChallengeController();
                    challengeController.closeInvitations(player.getUsername(), "OFFLINE");
                    challengeController.broadcastOnlineList();
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    // socket đã đóng
                }
            }
        }
    }

    /** Ghi message thành một dòng JSON; đồng bộ để hai luồng (luồng của client khác, bộ hẹn giờ) không ghi xen nhau. */
    public synchronized void send(Message msg) {
        if (writer == null) {
            return;
        }
        writer.print(msg.toJson());
        writer.print('\n');
        writer.flush();
    }
}
