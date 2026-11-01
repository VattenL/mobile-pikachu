package ptit.e23cnpm01.pikachu.view;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Locale;
import ptit.e23cnpm01.pikachu.R;
import ptit.e23cnpm01.pikachu.core.model.Player;
import ptit.e23cnpm01.pikachu.core.model.PlayerRanking;
import ptit.e23cnpm01.pikachu.core.protocol.Message;
import ptit.e23cnpm01.pikachu.core.protocol.MessageType;
import ptit.e23cnpm01.pikachu.network.ClientSession;
import ptit.e23cnpm01.pikachu.network.ServerConnection;

/**
 * Giao diện bảng xếp hạng, 10 người một trang; nút vị trí của tôi chỉ hiện khi đã đăng nhập.
 *
 * <p>Phụ trách: Dev 2.
 */
public class RankingActivity extends BaseActivity {
    /** Màu nền dòng của người chơi đang đăng nhập (Hình 3.6). */
    private static final int MY_ROW_COLOR = 0xFFFFF2CC;

    private RecyclerView rvRanking;
    private TextView tvPage;
    private Button btnPrevPage;
    private Button btnNextPage;
    private Button btnMyRank;
    private Button btnBack;
    private int page;
    private int pageCount;
    private ArrayList<PlayerRanking> listRanking;
    /** Thứ hạng của người chơi (me trong RANKING); dòng được tô theo tên đăng nhập. */
    private Integer myRank;

    /**
     * Ẩn nút Vị trí của tôi nếu chưa đăng nhập; gửi GET_RANKING với page = 1. Không kết nối được server thì báo
     * "Không kết nối được máy chủ" và quay về giao diện chính.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);
        rvRanking = findViewById(R.id.rvRanking);
        tvPage = findViewById(R.id.tvPage);
        btnPrevPage = findViewById(R.id.btnPrevPage);
        btnNextPage = findViewById(R.id.btnNextPage);
        btnMyRank = findViewById(R.id.btnMyRank);
        btnBack = findViewById(R.id.btnBack);
        ViewGroup header = findViewById(R.id.headerRow);
        for (int i = 0; i < header.getChildCount(); i++) {
            ((TextView) header.getChildAt(i)).setTypeface(null, Typeface.BOLD_ITALIC);
        }
        listRanking = new ArrayList<>();
        rvRanking.setLayoutManager(new LinearLayoutManager(this));
        rvRanking.setAdapter(new RankingAdapter());
        btnMyRank.setVisibility(ClientSession.player == null ? View.GONE : View.VISIBLE);

        if (!ServerConnection.getInstance().isConnected()) {
            Toast.makeText(this, R.string.cannot_connect, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        requestPage(1);
    }

    private void requestPage(int p) {
        ServerConnection.getInstance().send(new Message(MessageType.GET_RANKING).put("page", p));
    }

    public void onPrevPageClick(View v) {
        requestPage(page - 1);
    }

    public void onNextPageClick(View v) {
        requestPage(page + 1);
    }

    public void onMyRankClick(View v) {
        ServerConnection.getInstance().send(new Message(MessageType.GET_RANKING).put("mine", true));
    }

    /**
     * RANKING: hiện các dòng của trang và "Trang page/pageCount", lưu me vào myRank; bảng rỗng thì hiện "Chưa có
     * dữ liệu xếp hạng" và vô hiệu hai nút chuyển trang. Trả lời mine không có trang (người chơi chưa có trong
     * bảng) thì báo "Bạn chưa có trong bảng xếp hạng" và giữ nguyên trang đang xem.
     */
    @Override
    public void onMessage(Message msg) {
        if (!MessageType.RANKING.equals(msg.getType())) {
            super.onMessage(msg);
            return;
        }
        pageCount = msg.getInt("pageCount");
        myRank = msg.get("me", Integer.class);
        if (!msg.has("page")) {
            Toast.makeText(this, R.string.not_in_ranking, Toast.LENGTH_SHORT).show();
            return;
        }
        page = msg.getInt("page");
        ArrayList<PlayerRanking> rows = msg.get("listRanking",
                new TypeToken<ArrayList<PlayerRanking>>() { }.getType());
        listRanking.clear();
        if (rows != null) {
            listRanking.addAll(rows);
        }
        rvRanking.getAdapter().notifyDataSetChanged();
        findViewById(R.id.tvEmpty).setVisibility(listRanking.isEmpty() ? View.VISIBLE : View.GONE);
        tvPage.setText(getString(R.string.page_of, page, pageCount));
        btnPrevPage.setEnabled(!listRanking.isEmpty() && page > 1);
        btnNextPage.setEnabled(!listRanking.isEmpty() && page < pageCount);
        int mine = indexOfMyRow();
        if (mine >= 0) {
            rvRanking.scrollToPosition(mine);
        }
    }

    /** Quay lại giao diện chính. */
    public void onBackClick(View v) {
        finish();
    }

    /** Vị trí dòng của người chơi đang đăng nhập trên trang (theo tên đăng nhập, vì có thể đồng hạng), -1 nếu không có. */
    private int indexOfMyRow() {
        Player me = ClientSession.player;
        for (int i = 0; me != null && i < listRanking.size(); i++) {
            if (listRanking.get(i).getUsername().equals(me.getUsername())) {
                return i;
            }
        }
        return -1;
    }

    /** Tổng điểm bỏ phần .0 (12.5, 9). */
    private static String formatPoint(float point) {
        return point == Math.rint(point) ? String.valueOf((int) point) : String.valueOf(point);
    }

    /** Mili giây sang mm:ss, làm tròn tới giây; rỗng thì "--". */
    private String formatTime(Float millis) {
        if (millis == null) {
            return getString(R.string.no_win_time);
        }
        long seconds = Math.round(millis / 1000.0);
        return String.format(Locale.US, "%02d:%02d", seconds / 60, seconds % 60);
    }

    private class RankingAdapter extends RecyclerView.Adapter<RowHolder> {
        @NonNull
        @Override
        public RowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RowHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_ranking, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RowHolder holder, int position) {
            PlayerRanking pr = listRanking.get(position);
            holder.tvRank.setText(String.valueOf(pr.getRank()));
            holder.tvName.setText(pr.getUsername());
            holder.tvTotalPoint.setText(formatPoint(pr.getTotalPoint()));
            holder.tvAvgOpponentPoint.setText(String.format(Locale.US, "%.1f", pr.getAvgOpponentPoint()));
            holder.tvAvgWinTime.setText(formatTime(pr.getAvgWinTime()));
            boolean mine = position == indexOfMyRow();
            holder.itemView.setBackgroundColor(mine ? MY_ROW_COLOR : 0);
            int style = mine ? Typeface.BOLD : Typeface.NORMAL;
            for (TextView tv : new TextView[] {holder.tvRank, holder.tvName, holder.tvTotalPoint,
                    holder.tvAvgOpponentPoint, holder.tvAvgWinTime}) {
                tv.setTypeface(null, style);
            }
        }

        @Override
        public int getItemCount() {
            return listRanking.size();
        }
    }

    private static class RowHolder extends RecyclerView.ViewHolder {
        final TextView tvRank;
        final TextView tvName;
        final TextView tvTotalPoint;
        final TextView tvAvgOpponentPoint;
        final TextView tvAvgWinTime;

        RowHolder(View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvName = itemView.findViewById(R.id.tvName);
            tvTotalPoint = itemView.findViewById(R.id.tvTotalPoint);
            tvAvgOpponentPoint = itemView.findViewById(R.id.tvAvgOpponentPoint);
            tvAvgWinTime = itemView.findViewById(R.id.tvAvgWinTime);
        }
    }
}
