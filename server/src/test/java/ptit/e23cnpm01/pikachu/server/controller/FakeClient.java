package ptit.e23cnpm01.pikachu.server.controller;

import java.util.ArrayList;
import ptit.e23cnpm01.pikachu.core.logic.BoardGenerator;
import ptit.e23cnpm01.pikachu.core.model.Board;
import ptit.e23cnpm01.pikachu.core.model.Player;
import ptit.e23cnpm01.pikachu.core.protocol.Message;

/** ClientHandler không có socket, giữ lại các message được gửi (đi qua chuỗi JSON như trên mạng). */
class FakeClient extends ClientHandler {
    final ArrayList<Message> sent = new ArrayList<>();

    FakeClient(int id, String username) {
        super(null);
        Player p = new Player();
        p.setId(id);
        p.setUsername(username);
        setPlayer(p);
    }

    /** Tạo và đưa vào danh sách online (trạng thái rỗi). */
    static FakeClient online(int id, String username) {
        FakeClient c = new FakeClient(id, username);
        OnlinePlayerManager.getInstance().addOnlinePlayer(c);
        return c;
    }

    @Override
    public synchronized void send(Message msg) {
        sent.add(Message.fromJson(msg.toJson()));
    }

    /** Các message loại type đã nhận. */
    synchronized ArrayList<Message> of(String type) {
        ArrayList<Message> out = new ArrayList<>();
        for (Message m : sent) {
            if (m.getType().equals(type)) {
                out.add(m);
            }
        }
        return out;
    }

    /** Message loại type nhận sau cùng, null nếu chưa có. */
    Message last(String type) {
        ArrayList<Message> list = of(type);
        return list.isEmpty() ? null : list.get(list.size() - 1);
    }

    /** Gỡ khỏi danh sách online và gỡ mọi lời mời của người này. */
    void leave() {
        OnlinePlayerManager.getInstance().removeOnlinePlayer(this);
        OnlinePlayerManager.getInstance().removeInvitationsOf(getPlayer().getUsername());
    }

    /** Phần core của Dev 1 mà việc tạo ván cần (sinh đề, sao bàn) đã có chưa. */
    static boolean coreReady() {
        try {
            Board b = BoardGenerator.generateBoard(10, 10, 25, 1L);
            b.copy();
            return true;
        } catch (UnsupportedOperationException e) {
            return false;
        }
    }
}
