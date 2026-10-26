package ptit.e23cnpm01.pikachu.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import ptit.e23cnpm01.pikachu.core.model.PlayerRanking;
import ptit.e23cnpm01.pikachu.core.protocol.Message;

/**
 * Giao diện bảng xếp hạng.
 *
 * <p>Phụ trách: Dev 2.
 */
public class RankingActivity extends BaseActivity {
    private RecyclerView rvRanking;
    private Button btnBack;
    private ArrayList<PlayerRanking> listRanking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: setContentView, gắn view
    }

    @Override
    public void onMessage(Message msg) {
        // TODO
    }

    public void onBackClick(View v) {
        // TODO
    }
}
