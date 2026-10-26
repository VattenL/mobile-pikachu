package ptit.e23cnpm01.pikachu.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import ptit.e23cnpm01.pikachu.core.model.Player;
import ptit.e23cnpm01.pikachu.core.model.PlayerRanking;
import ptit.e23cnpm01.pikachu.core.protocol.Message;

/**
 * Màn hình đấu đối kháng: danh sách người chơi online, thách đấu. Khi mở thì gửi GET_ONLINE_LIST.
 *
 * <p>Phụ trách: Dev 2.
 */
public class LobbyActivity extends BaseActivity {
    private RecyclerView rvOnlinePlayers;
    private TextView tvMessage;
    private Button btnBack;
    private ArrayList<PlayerRanking> onlinePlayers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: setContentView, gắn view, gửi GET_ONLINE_LIST
    }

    public void onPlayerClick(Player opponent) {
        // TODO
    }

    public void onBackClick(View v) {
        // TODO
    }

    @Override
    public void onMessage(Message msg) {
        // TODO
    }
}
