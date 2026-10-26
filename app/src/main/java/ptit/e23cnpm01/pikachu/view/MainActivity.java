package ptit.e23cnpm01.pikachu.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import ptit.e23cnpm01.pikachu.core.model.Player;
import ptit.e23cnpm01.pikachu.core.protocol.Message;

/**
 * Giao diện chính. Dev 1: đăng xuất, nút chơi với máy; Dev 2: danh sách online, thách đấu, nút xếp hạng.
 *
 * <p>Phụ trách: Dev 1 và Dev 2.
 */
public class MainActivity extends BaseActivity {
    private TextView tvPlayer;
    private Button btnLogout;
    private Player player;
    private RecyclerView rvOnlinePlayers;
    private Button btnRanking;
    private Button btnPlayWithBot;
    private TextView tvMessage;
    private ArrayList<Player> onlinePlayers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: setContentView, gắn view
    }

    public void onLogoutClick(View v) {
        // TODO
    }

    public void onRankingClick(View v) {
        // TODO
    }

    public void onPlayWithBotClick(View v) {
        // TODO
    }

    public void onPlayerClick(Player opponent) {
        // TODO
    }

    @Override
    public void onMessage(Message msg) {
        // TODO
    }
}
