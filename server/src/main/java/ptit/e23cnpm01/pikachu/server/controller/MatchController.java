package ptit.e23cnpm01.pikachu.server.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import ptit.e23cnpm01.pikachu.core.logic.BoardGenerator;
import ptit.e23cnpm01.pikachu.core.model.Board;
import ptit.e23cnpm01.pikachu.core.model.Match;
import ptit.e23cnpm01.pikachu.core.model.MatchResult;
import ptit.e23cnpm01.pikachu.core.model.Player;
import ptit.e23cnpm01.pikachu.core.protocol.Message;
import ptit.e23cnpm01.pikachu.core.protocol.MessageType;
import ptit.e23cnpm01.pikachu.server.dao.MatchDAO;

/**
 * Trọng tài ván đối kháng. Dev 1: handleMove(); Dev 2: phần còn lại (kết thúc ván, hết giờ, thoát, mất kết nối, tái đấu).
 *
 * <p>Mọi hàm xử lý khóa MatchRoom và bỏ qua message nếu người gửi không thuộc phòng roomId (handler.roomId khác
 * roomId: chưa từng ở phòng đó hoặc đã rời phòng). Người chơi còn ở phòng từ lúc vào ván cho tới khi chọn Dừng (kể cả
 * thoát, hết thời gian trả lời, mất kết nối) hoặc phòng bị bỏ.
 *
 * <p>Phụ trách: Dev 1 và Dev 2.
 */
public class MatchController {
    /** Thời gian chờ trả lời tái đấu sau MATCH_END, giây. */
    public static final int REMATCH_REPLY_SEC = 30;

    /**
     * Nước đi MOVE (Dev 1). Bàn của người gửi hết ô thì gọi endMatch(room, "CLEARED", handler) ngay trong khối khóa
     * phòng; endMatch() tự ghi finishTime theo đồng hồ server.
     */
    public void handleMove(ClientHandler handler, Message msg) {
        // TODO
    }

    /** Người gửi bấm Thoát giữa ván: kết thúc ván với lý do QUIT, người gửi thua. */
    public void handleLeaveMatch(ClientHandler handler, Message msg) {
        MatchRoom room = roomOf(msg);
        if (room == null) {
            return;
        }
        synchronized (room) {
            if (!inRoom(room, handler) || room.isEnded()) {
                return;
            }
            endMatch(room, "QUIT", handler);
        }
    }

    /**
     * Ghi lựa chọn tái đấu của người gửi vào rematchReplies (gửi lại được để đổi từ đồng ý sang từ chối). Từ chối:
     * stopRematch(). Cả hai đồng ý: bỏ phòng cũ, sinh đề mới, tạo phòng mới và gửi MATCH_START cho hai người; trạng
     * thái vẫn là bận. Bỏ qua nếu ván chưa kết thúc, hoặc đồng ý khi ván không hỏi tái đấu (đối thủ đã thoát).
     */
    public void handleRematchReply(ClientHandler handler, Message msg) {
        MatchRoom room = roomOf(msg);
        if (room == null) {
            return;
        }
        synchronized (room) {
            if (!inRoom(room, handler) || !room.isEnded()) {
                return;
            }
            boolean accept = msg.getBoolean("accept");
            if (accept && !isRematchOffered(room)) {
                return;
            }
            Boolean[] replies = room.getRematchReplies();
            replies[indexOf(room, handler)] = accept;
            if (!accept) {
                stopRematch(room, handler);
                return;
            }
            for (Boolean reply : replies) {
                if (!Boolean.TRUE.equals(reply)) {
                    return;
                }
            }
            ClientHandler[] handlers = room.getHandlers();
            MatchManager manager = MatchManager.getInstance();
            manager.removeRoom(room.getRoomId());
            Board board = BoardGenerator.generateBoard(ChallengeController.ROWS, ChallengeController.COLS,
                    ChallengeController.ICON_TYPES, new Random().nextLong());
            MatchRoom newRoom = manager.createMatch(handlers[0], handlers[1], board);
            int[][] tiles = newRoom.getMatch().getBoard().getTiles();
            int timeLimitSec = newRoom.getMatch().getTimeLimit();
            handlers[0].send(ChallengeController.matchStart(newRoom.getRoomId(), tiles, timeLimitSec,
                    handlers[1].getPlayer().getUsername()));
            handlers[1].send(ChallengeController.matchStart(newRoom.getRoomId(), tiles, timeLimitSec,
                    handlers[0].getPlayer().getUsername()));
        }
    }

    /** Hẹn giờ 10 phút của phòng: kết thúc ván hòa với lý do TIMEOUT (không làm gì nếu ván đã kết thúc). */
    public void onTimeout(MatchRoom room) {
        synchronized (room) {
            endMatch(room, "TIMEOUT", null);
        }
    }

    /**
     * Hết 30 giây sau MATCH_END mà chưa đủ hai người đồng ý: những người còn trong phòng mà chưa trả lời được coi là
     * chọn Dừng. Phòng đã bị bỏ (tái đấu, mọi người đã dừng) thì không làm gì.
     */
    public void onRematchTimeout(MatchRoom room) {
        synchronized (room) {
            if (MatchManager.getInstance().getRoom(room.getRoomId()) != room) {
                return;
            }
            ClientHandler[] handlers = room.getHandlers();
            for (int i = 0; i < handlers.length; i++) {
                if (inRoom(room, handlers[i]) && room.getRematchReplies()[i] == null) {
                    stopRematch(room, handlers[i]);
                }
            }
        }
    }

    /**
     * Kết nối của handler bị mất khi còn ở phòng: ván đang diễn ra thì kết thúc với lý do DISCONNECT; đang ở dialog
     * kết quả thì coi như chọn Dừng. Trong lúc chờ khóa phòng, hai người có thể vừa đồng ý tái đấu (handler đã sang
     * phòng mới), nên đọc lại roomId cho tới khi khóa đúng phòng handler đang ở.
     */
    public void handleDisconnect(ClientHandler handler) {
        while (true) {
            Integer roomId = handler.getRoomId();
            MatchRoom room = roomId == null ? null : MatchManager.getInstance().getRoom(roomId);
            if (room == null) {
                return;
            }
            synchronized (room) {
                if (!inRoom(room, handler)) {
                    continue;
                }
                if (!room.isEnded()) {
                    endMatch(room, "DISCONNECT", handler);
                } else {
                    stopRematch(room, handler);
                }
                return;
            }
        }
    }

    /**
     * Kết thúc ván, chỉ chạy một lần cho mỗi phòng. player là người xóa hết ô (CLEARED) hoặc người thoát, mất kết nối
     * (QUIT, DISCONNECT); rỗng khi hết giờ (TIMEOUT). Hủy hẹn giờ 10 phút, xác định kết quả theo bảng ở pha 3, mục 8d
     * (finishTime của người thắng CLEARED là lúc này trừ lúc bắt đầu ván), lưu bằng MatchDAO.saveMatch(), gửi
     * MATCH_END: CLEARED, TIMEOUT cho hai người (rematch = true); QUIT, DISCONNECT chỉ cho người còn lại (rematch =
     * false) rồi stopRematch() người thoát. Hẹn 30 giây chờ trả lời tái đấu bằng một Timer mới của phòng (Timer đã hủy
     * không hẹn lại được).
     */
    private void endMatch(MatchRoom room, String reason, ClientHandler player) {
        if (room.isEnded()) {
            return;
        }
        room.setEnded(true);
        room.setEndReason(reason);
        room.getTimer().cancel();
        long now = System.currentTimeMillis();
        boolean rematch = isRematchOffered(room);
        ClientHandler[] handlers = room.getHandlers();
        MatchResult[] results = new MatchResult[handlers.length];
        for (int i = 0; i < handlers.length; i++) {
            ClientHandler h = handlers[i];
            MatchResult r = new MatchResult();
            r.setPlayer(h.getPlayer());
            r.setBoard(room.getBoard(h));
            if ("TIMEOUT".equals(reason)) {
                r.setResult("DRAW");
            } else if ("CLEARED".equals(reason)) {
                r.setResult(h == player ? "WIN" : "LOSE");
                if (h == player) {
                    r.setFinishTime((int) (now - room.getStartMillis()));
                }
            } else {
                r.setResult(h == player ? "LOSE" : "WIN");
            }
            r.setPoint(point(r.getResult()));
            results[i] = r;
        }
        Match match = room.getMatch();
        match.setEndTime(new Date(now));
        match.setResults(results);
        if (!new MatchDAO().saveMatch(match)) {
            System.err.println("Could not save the match of room " + room.getRoomId());
        }
        for (int i = 0; i < handlers.length; i++) {
            if (rematch || handlers[i] != player) {
                handlers[i].send(matchEnd(room.getRoomId(), results[i], results[1 - i], reason, rematch));
            }
        }
        Timer timer = new Timer("room-" + room.getRoomId() + "-rematch", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                new MatchController().onRematchTimeout(room);
            }
        }, REMATCH_REPLY_SEC * 1000L);
        room.setTimer(timer);
        if (!rematch) {
            stopRematch(room, player);
        }
    }

    /**
     * Người stopper dừng (chọn Dừng, Về sảnh, thoát, hết giờ trả lời hoặc mất kết nối ở dialog kết quả): về rỗi và rời
     * phòng. Ván kết thúc bình thường (có hỏi tái đấu) mà người kia còn ở phòng thì người kia cũng về rỗi, nhận
     * OPPONENT_LEFT và rời phòng; đối thủ đã thoát thì người còn lại vẫn bận tới khi bấm Về sảnh. Phòng không còn ai thì
     * bỏ phòng; cuối cùng gửi lại danh sách online.
     */
    private void stopRematch(MatchRoom room, ClientHandler stopper) {
        room.getRematchReplies()[indexOf(room, stopper)] = false;
        ArrayList<Player> idle = new ArrayList<>();
        idle.add(stopper.getPlayer());
        stopper.setRoomId(null);
        ClientHandler other = room.getOpponent(stopper);
        boolean otherLeaves = isRematchOffered(room) && inRoom(room, other);
        if (otherLeaves) {
            idle.add(other.getPlayer());
            other.setRoomId(null);
        }
        OnlinePlayerManager.getInstance().updateStatus(idle, "IDLE");
        if (otherLeaves) {
            other.send(new Message(MessageType.OPPONENT_LEFT));
        }
        if (!inRoom(room, other)) {
            MatchManager.getInstance().removeRoom(room.getRoomId());
        }
        new ChallengeController().broadcastOnlineList();
    }

    /** Phòng theo trường roomId của message, null nếu không có trường đó hoặc phòng không còn. */
    private static MatchRoom roomOf(Message msg) {
        return msg.has("roomId") ? MatchManager.getInstance().getRoom(msg.getInt("roomId")) : null;
    }

    /** handler còn ở phòng: roomId của handler đúng bằng mã phòng. */
    private static boolean inRoom(MatchRoom room, ClientHandler handler) {
        return Objects.equals(handler.getRoomId(), room.getRoomId());
    }

    private static int indexOf(MatchRoom room, ClientHandler handler) {
        return room.getHandlers()[0] == handler ? 0 : 1;
    }

    /** Ván kết thúc bằng CLEARED hoặc TIMEOUT thì hai người được hỏi tái đấu. */
    private static boolean isRematchOffered(MatchRoom room) {
        return "CLEARED".equals(room.getEndReason()) || "TIMEOUT".equals(room.getEndReason());
    }

    /** Điểm của ván theo đề: thắng 1, hòa 0.5, thua 0. */
    private static float point(String result) {
        if ("WIN".equals(result)) {
            return 1f;
        }
        return "DRAW".equals(result) ? 0.5f : 0f;
    }

    /**
     * MATCH_END cho người có kết quả mine: roomId, result, point, finishTime, remaining của người nhận và
     * opponentFinishTime, opponentRemaining của đối thủ, reason, rematch, replySec. finishTime tính bằng mili giây,
     * rỗng nếu người đó chưa xóa hết ô.
     */
    private static Message matchEnd(int roomId, MatchResult mine, MatchResult opponent, String reason,
            boolean rematch) {
        return new Message(MessageType.MATCH_END).put("roomId", roomId)
                .put("result", mine.getResult()).put("point", mine.getPoint())
                .put("finishTime", mine.getFinishTime()).put("remaining", mine.getBoard().countRemaining())
                .put("opponentFinishTime", opponent.getFinishTime())
                .put("opponentRemaining", opponent.getBoard().countRemaining())
                .put("reason", reason).put("rematch", rematch).put("replySec", REMATCH_REPLY_SEC);
    }
}
