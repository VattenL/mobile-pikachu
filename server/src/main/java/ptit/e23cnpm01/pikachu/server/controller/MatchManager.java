package ptit.e23cnpm01.pikachu.server.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import ptit.e23cnpm01.pikachu.core.model.Board;
import ptit.e23cnpm01.pikachu.core.model.Match;

/**
 * Giữ các ván đang diễn ra theo mã phòng roomId (số tăng dần trong bộ nhớ; Match.id chỉ có khi lưu lúc kết thúc).
 *
 * <p>Phụ trách: Dev 2.
 */
public class MatchManager {
    /** Thời gian mỗi ván, giây (plan.md §8). */
    public static final int TIME_LIMIT_SEC = 600;

    private static MatchManager instance;

    private HashMap<Integer, MatchRoom> rooms;
    private int nextRoomId;

    private MatchManager() {
        rooms = new HashMap<>();
        nextRoomId = 1;
    }

    public static synchronized MatchManager getInstance() {
        if (instance == null) {
            instance = new MatchManager();
        }
        return instance;
    }

    /**
     * Cấp roomId mới, tạo Match (giờ bắt đầu, 10 phút, đề bài) và MatchRoom giữ hai bản sao đề bài cho a và b, hẹn
     * giờ 10 phút gọi MatchController.onTimeout(), gán roomId cho ClientHandler của hai người.
     */
    public synchronized MatchRoom createMatch(ClientHandler a, ClientHandler b, Board board) {
        int roomId = nextRoomId++;
        Match match = new Match();
        match.setStartTime(new Date());
        match.setTimeLimit(TIME_LIMIT_SEC);
        match.setBoard(board);
        MatchRoom room = new MatchRoom(roomId, match, new ClientHandler[] {a, b},
                new Board[] {board.copy(), board.copy()});
        Timer timer = new Timer("room-" + roomId, true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                new MatchController().onTimeout(room);
            }
        }, TIME_LIMIT_SEC * 1000L);
        room.setTimer(timer);
        rooms.put(roomId, room);
        a.setRoomId(roomId);
        b.setRoomId(roomId);
        return room;
    }

    public synchronized MatchRoom getRoom(int roomId) {
        return rooms.get(roomId);
    }

    /** Bỏ phòng, hủy hẹn giờ của phòng, xóa roomId khỏi các ClientHandler còn giữ nó. */
    public synchronized void removeRoom(int roomId) {
        MatchRoom room = rooms.remove(roomId);
        if (room == null) {
            return;
        }
        if (room.getTimer() != null) {
            room.getTimer().cancel();
        }
        for (ClientHandler h : room.getHandlers()) {
            if (Objects.equals(h.getRoomId(), roomId)) {
                h.setRoomId(null);
            }
        }
    }
}
