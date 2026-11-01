package ptit.e23cnpm01.pikachu.server.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Objects;
import ptit.e23cnpm01.pikachu.core.model.Player;
import ptit.e23cnpm01.pikachu.core.model.PlayerRanking;
import ptit.e23cnpm01.pikachu.core.protocol.Message;
import ptit.e23cnpm01.pikachu.core.protocol.MessageType;
import ptit.e23cnpm01.pikachu.server.dao.PlayerRankingDAO;

/**
 * Xử lý GET_RANKING: cắt trang (page) hoặc tìm trang chứa người chơi đang đăng nhập (mine).
 *
 * <p>Phụ trách: Dev 2.
 */
public class RankingController {
    private static final int PAGE_SIZE = 10;

    /**
     * Lấy cả bảng từ PlayerRankingDAO.getRanking(), đánh số hạng (trùng cả ba tiêu chí thì cùng hạng, kiểu 1, 2, 2,
     * 4) rồi gửi RANKING cho người hỏi:
     * <ul>
     * <li>mine = true: trang chứa dòng của người chơi đang đăng nhập trên kết nối này, kèm me là thứ hạng của họ.
     * Khách hoặc người chưa có trong bảng: me, page rỗng và listRanking rỗng (client giữ nguyên trang đang xem).
     * <li>ngược lại: trang page, ngoài khoảng thì kẹp về 1..pageCount.
     * </ul>
     * Mỗi dòng của listRanking chỉ gồm rank, username, totalPoint, avgOpponentPoint, avgWinTime (rỗng nếu chưa thắng
     * ván nào có thời gian hoàn thành). pageCount = ⌈số dòng / 10⌉, tối thiểu 1.
     */
    public void handleGetRanking(ClientHandler handler, Message msg) {
        ArrayList<PlayerRanking> listRanking = new PlayerRankingDAO().getRanking();
        for (int i = 0; i < listRanking.size(); i++) {
            PlayerRanking pr = listRanking.get(i);
            PlayerRanking prev = i > 0 ? listRanking.get(i - 1) : null;
            if (prev != null && prev.getTotalPoint() == pr.getTotalPoint()
                    && prev.getAvgOpponentPoint() == pr.getAvgOpponentPoint()
                    && Objects.equals(prev.getAvgWinTime(), pr.getAvgWinTime())) {
                pr.setRank(prev.getRank());
            } else {
                pr.setRank(i + 1);
            }
        }
        int pageCount = Math.max(1, (listRanking.size() + PAGE_SIZE - 1) / PAGE_SIZE);

        Message res = new Message(MessageType.RANKING);
        Integer page = null;
        if (msg.getBoolean("mine")) {
            Player player = handler.getPlayer();
            for (int i = 0; player != null && i < listRanking.size(); i++) {
                if (listRanking.get(i).getUsername().equals(player.getUsername())) {
                    page = i / PAGE_SIZE + 1;
                    res.put("me", listRanking.get(i).getRank());
                    break;
                }
            }
        } else {
            page = Math.min(Math.max(msg.getInt("page"), 1), pageCount);
        }

        JsonArray rows = new JsonArray();
        if (page != null) {
            int to = Math.min(page * PAGE_SIZE, listRanking.size());
            for (int i = (page - 1) * PAGE_SIZE; i < to; i++) {
                PlayerRanking pr = listRanking.get(i);
                JsonObject row = new JsonObject();
                row.addProperty("rank", pr.getRank());
                row.addProperty("username", pr.getUsername());
                row.addProperty("totalPoint", pr.getTotalPoint());
                row.addProperty("avgOpponentPoint", pr.getAvgOpponentPoint());
                row.addProperty("avgWinTime", pr.getAvgWinTime());
                rows.add(row);
            }
            res.put("page", page);
        }
        res.put("pageCount", pageCount);
        res.put("listRanking", rows);
        handler.send(res);
    }
}
