package ptit.e23cnpm01.pikachu.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import ptit.e23cnpm01.pikachu.core.model.PlayerRanking;
import ptit.e23cnpm01.pikachu.core.protocol.Message;

/**
 * Giao diện bảng xếp hạng, 10 người một trang; nút vị trí của tôi chỉ hiện khi đã đăng nhập.
 *
 * <p>Phụ trách: Dev 2.
 */
public class RankingActivity extends BaseActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: setContentView, gắn view
    }

    public void onPrevPageClick(View v) {
        // TODO
    }

    public void onNextPageClick(View v) {
        // TODO
    }

    public void onMyRankClick(View v) {
        // TODO
    }

    @Override
    public void onMessage(Message msg) {
        // TODO
    }

    public void onBackClick(View v) {
        // TODO
    }
}
