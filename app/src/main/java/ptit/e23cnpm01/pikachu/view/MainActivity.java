package ptit.e23cnpm01.pikachu.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import ptit.e23cnpm01.pikachu.core.model.Player;
import ptit.e23cnpm01.pikachu.core.protocol.Message;

/**
 * Giao diện chính, mở ngay khi vào ứng dụng, dùng cho cả khách và người chơi: Chơi với máy, Đấu đối kháng,
 * Xem bảng xếp hạng, Cài đặt. Dev 1: đăng xuất, nút chơi với máy; Dev 2: nút đấu đối kháng (mở LobbyActivity,
 * chưa đăng nhập thì qua LoginActivity trước), nút xếp hạng.
 *
 * <p>Phụ trách: Dev 1 và Dev 2.
 */
public class MainActivity extends BaseActivity {
    private TextView tvPlayer;
    private Button btnLogout;
    private Player player;
    private Button btnPlayWithBot;
    private Button btnMatch;
    private Button btnRanking;
    private Button btnSettings;

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

    public void onMatchClick(View v) {
        // TODO
    }

    @Override
    public void onMessage(Message msg) {
        // TODO
    }
}
