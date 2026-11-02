package ptit.e23cnpm01.pikachu.server.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import ptit.e23cnpm01.pikachu.core.logic.BoardGenerator;
import ptit.e23cnpm01.pikachu.core.model.Board;
import ptit.e23cnpm01.pikachu.core.model.Player;
import ptit.e23cnpm01.pikachu.core.model.PlayerRanking;
import ptit.e23cnpm01.pikachu.core.protocol.Message;
import ptit.e23cnpm01.pikachu.core.protocol.MessageType;
import ptit.e23cnpm01.pikachu.server.dao.PlayerRankingDAO;

/**
 * Xử lý GET_ONLINE_LIST, INVITE, INVITE_CANCEL, INVITE_REPLY, UPDATE_STATUS; hết hạn lời mời; gửi lại danh sách online.
 *
 * <p>Lời mời chỉ nằm trong bộ nhớ (OnlinePlayerManager); lớp này kiểm tra lời mời còn hiệu lực rồi báo cho hai bên.
 *
 * <p>Phụ trách: Dev 2.
 */
public class ChallengeController {
    /** Thời gian chờ trả lời một lời mời, giây. */
    public static final int INVITE_TIMEOUT_SEC = 30;
    /** Đề bài 10x10 với 25 loại hình, mỗi loại 4 ô (plan.md §8); MatchController dùng lại khi tái đấu. */
    static final int ROWS = 10;
    static final int COLS = 10;
    static final int ICON_TYPES = 25;

    /**
     * Hẹn giờ 30 giây cho các lời mời đang chờ. Tĩnh vì lớp điều khiển được tạo bằng new ở mỗi lần xử lý message,
     * mọi lời mời dùng chung một bộ hẹn giờ.
     */
    private static Timer inviteTimer = new Timer("invite-timer", true);
    /**
     * Tác vụ hẹn giờ của lời mời đi hiện tại của mỗi người mời. Lời mời cũ đã trả lời hoặc hủy vẫn còn tác vụ trong
     * inviteTimer; nếu người đó mời lại cùng người kia trước khi tác vụ cũ chạy, tác vụ cũ không còn là tác vụ hiện
     * tại nên bỏ qua, không gỡ nhầm lời mời mới.
     */
    private static final ConcurrentHashMap<String, TimerTask> inviteTasks = new ConcurrentHashMap<>();

    /** Gửi ONLINE_LIST cho mọi người chơi đang rỗi; danh sách gửi cho mỗi người không gồm chính người đó. */
    public void broadcastOnlineList() {
        OnlinePlayerManager manager = OnlinePlayerManager.getInstance();
        ArrayList<Player> players = manager.getOnlinePlayers();
        ArrayList<PlayerRanking> totals = new PlayerRankingDAO().getTotalPoints(players);
        for (Player p : players) {
            if ("IDLE".equals(p.getStatus())) {
                ClientHandler handler = manager.getHandler(p.getUsername());
                if (handler != null) {
                    handler.send(onlineList(totals, p.getUsername()));
                }
            }
        }
    }

    /** Gửi danh sách online hiện tại cho riêng người hỏi. */
    public void handleGetOnlineList(ClientHandler handler, Message msg) {
        ArrayList<Player> players = OnlinePlayerManager.getInstance().getOnlinePlayers();
        ArrayList<PlayerRanking> totals = new PlayerRankingDAO().getTotalPoints(players);
        handler.send(onlineList(totals, handler.getPlayer().getUsername()));
    }

    /**
     * Bỏ qua nếu người mời đang bận, đã có một lời mời đi đang chờ hoặc tự mời mình. Người được mời không online hoặc
     * đang bận thì báo INVITE_RESULT cho người mời; ngược lại hẹn giờ 30 giây và gửi INVITE_RECEIVED cho người được mời.
     */
    public void handleInvite(ClientHandler handler, Message msg) {
        OnlinePlayerManager manager = OnlinePlayerManager.getInstance();
        Player me = handler.getPlayer();
        String to = msg.getString("to");
        if (to == null || to.equals(me.getUsername()) || "BUSY".equals(me.getStatus())
                || manager.hasInvitation(me.getUsername())) {
            return;
        }
        String from = me.getUsername();
        String reason = manager.sendInvitation(from, to);
        ClientHandler target = manager.getHandler(to);
        if (reason == null && target == null) {
            // người được mời vừa rời mạng giữa hai lời gọi
            manager.removeInvitation(from, to);
            reason = "OFFLINE";
        }
        if (reason != null) {
            handler.send(inviteResult(to, false, reason));
            return;
        }
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (inviteTasks.remove(from, this)) {
                    onInviteTimeout(from, to);
                }
            }
        };
        inviteTasks.put(from, task);
        inviteTimer.schedule(task, INVITE_TIMEOUT_SEC * 1000L);
        target.send(new Message(MessageType.INVITE_RECEIVED).put("from", from).put("timeoutSec", INVITE_TIMEOUT_SEC));
    }

    /** Người mời hủy lời mời đang chờ: gửi INVITE_CLOSED (CANCELLED) cho người được mời. */
    public void handleInviteCancel(ClientHandler handler, Message msg) {
        String from = handler.getPlayer().getUsername();
        String to = msg.getString("to");
        if (to != null && OnlinePlayerManager.getInstance().removeInvitation(from, to)) {
            send(to, inviteClosed(from, "CANCELLED"));
        }
    }

    /**
     * Hết 30 giây: nếu lời mời vẫn còn thì gỡ, gửi INVITE_RESULT (EXPIRED) cho người mời và INVITE_CLOSED (EXPIRED) cho
     * người được mời; lời mời đã được xử lý thì không làm gì.
     */
    public void onInviteTimeout(String from, String to) {
        if (OnlinePlayerManager.getInstance().removeInvitation(from, to)) {
            send(from, inviteResult(to, false, "EXPIRED"));
            send(to, inviteClosed(from, "EXPIRED"));
        }
    }

    /** Đóng mọi lời mời đi và đến username với lý do reason (BUSY khi vào ván, OFFLINE khi rời mạng). */
    public void closeInvitations(String username, String reason) {
        for (Map.Entry<String, String> e : OnlinePlayerManager.getInstance().removeInvitationsOf(username).entrySet()) {
            String inviter = e.getKey();
            String invitee = e.getValue();
            if (inviter.equals(username)) {
                send(invitee, inviteClosed(inviter, reason));
            } else {
                send(inviter, inviteResult(invitee, false, reason));
            }
        }
    }

    /**
     * Người được mời trả lời lời mời của from. Lời mời không còn hiệu lực: chỉ gửi INVITE_CLOSED (EXPIRED) cho người
     * trả lời. Từ chối: INVITE_RESULT (REJECTED) cho người mời. Chấp nhận: kiểm tra lại hai người chưa bận, sinh đề,
     * tạo phòng, chuyển hai người sang bận, đóng mọi lời mời khác của hai người (BUSY), gửi MATCH_START cho hai người
     * rồi gửi lại danh sách online.
     */
    public void handleInviteReply(ClientHandler handler, Message msg) {
        OnlinePlayerManager manager = OnlinePlayerManager.getInstance();
        Player me = handler.getPlayer();
        String from = msg.getString("from");
        if (from == null || !manager.removeInvitation(from, me.getUsername())) {
            handler.send(inviteClosed(from, "EXPIRED"));
            return;
        }
        if (!msg.getBoolean("accept")) {
            send(from, inviteResult(me.getUsername(), false, "REJECTED"));
            return;
        }
        MatchRoom room;
        ClientHandler inviter;
        // kiểm tra và chuyển trạng thái trong cùng một khối đồng bộ: hai lời chấp nhận cùng lúc với cùng một người
        // thì chỉ một lời tạo được ván
        synchronized (manager) {
            inviter = manager.getHandler(from);
            if (inviter == null) {
                handler.send(inviteClosed(from, "OFFLINE"));
                return;
            }
            if ("BUSY".equals(inviter.getPlayer().getStatus()) || "BUSY".equals(me.getStatus())) {
                handler.send(inviteClosed(from, "BUSY"));
                return;
            }
            Board board = BoardGenerator.generateBoard(ROWS, COLS, ICON_TYPES, new Random().nextLong());
            room = MatchManager.getInstance().createMatch(inviter, handler, board);
            manager.updateStatus(new ArrayList<>(Arrays.asList(inviter.getPlayer(), me)), "BUSY");
        }
        closeInvitations(from, "BUSY");
        closeInvitations(me.getUsername(), "BUSY");
        int[][] tiles = room.getMatch().getBoard().getTiles();
        int timeLimitSec = room.getMatch().getTimeLimit();
        inviter.send(matchStart(room.getRoomId(), tiles, timeLimitSec, me.getUsername()));
        handler.send(matchStart(room.getRoomId(), tiles, timeLimitSec, from));
        broadcastOnlineList();
    }

    /**
     * UPDATE_STATUS từ client (TRAINING khi bắt đầu ván với máy, IDLE khi ván kết thúc hoặc thoát): bỏ qua nếu người
     * gửi đang bận hoặc trạng thái không hợp lệ; ngược lại cập nhật rồi gửi lại danh sách online.
     */
    public void handleUpdateStatus(ClientHandler handler, Message msg) {
        Player me = handler.getPlayer();
        String status = msg.getString("status");
        if ("BUSY".equals(me.getStatus()) || !("IDLE".equals(status) || "TRAINING".equals(status))) {
            return;
        }
        OnlinePlayerManager.getInstance().updateStatus(new ArrayList<>(Arrays.asList(me)), status);
        broadcastOnlineList();
    }

    /**
     * ONLINE_LIST cho receiver: trường onlinePlayers, mỗi dòng đúng username, status, totalPoint; không gồm receiver;
     * sắp theo tổng điểm giảm dần rồi theo tên.
     */
    private static Message onlineList(ArrayList<PlayerRanking> totals, String receiver) {
        ArrayList<PlayerRanking> sorted = new ArrayList<>(totals);
        sorted.sort(Comparator.comparing(PlayerRanking::getTotalPoint).reversed()
                .thenComparing(PlayerRanking::getUsername));
        JsonArray rows = new JsonArray();
        for (PlayerRanking pr : sorted) {
            if (pr.getUsername().equals(receiver)) {
                continue;
            }
            JsonObject row = new JsonObject();
            row.addProperty("username", pr.getUsername());
            row.addProperty("status", pr.getStatus());
            row.addProperty("totalPoint", pr.getTotalPoint());
            rows.add(row);
        }
        return new Message(MessageType.ONLINE_LIST).put("onlinePlayers", rows);
    }

    private static Message inviteResult(String to, boolean accepted, String reason) {
        return new Message(MessageType.INVITE_RESULT).put("to", to).put("accepted", accepted).put("reason", reason);
    }

    private static Message inviteClosed(String from, String reason) {
        return new Message(MessageType.INVITE_CLOSED).put("from", from).put("reason", reason);
    }

    /**
     * MATCH_START: roomId, tiles (lưới 12x12 của đề bài), timeLimitSec, opponent chỉ gồm username. MatchController
     * dùng lại khi tái đấu.
     */
    static Message matchStart(int roomId, int[][] tiles, int timeLimitSec, String opponent) {
        JsonObject opp = new JsonObject();
        opp.addProperty("username", opponent);
        return new Message(MessageType.MATCH_START).put("roomId", roomId).put("tiles", tiles)
                .put("timeLimitSec", timeLimitSec).put("opponent", opp);
    }

    /** Gửi msg cho người chơi username nếu người đó còn online. */
    private static void send(String username, Message msg) {
        ClientHandler handler = OnlinePlayerManager.getInstance().getHandler(username);
        if (handler != null) {
            handler.send(msg);
        }
    }
}
