package ptit.e23cnpm01.pikachu.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import ptit.e23cnpm01.pikachu.R;
import ptit.e23cnpm01.pikachu.core.model.Player;
import ptit.e23cnpm01.pikachu.core.protocol.Message;
import ptit.e23cnpm01.pikachu.network.ClientSession;
import ptit.e23cnpm01.pikachu.network.ServerConnection;

/**
 * Giao diện chính, mở ngay khi vào ứng dụng, dùng cho cả khách và người chơi: Chơi với máy, Đấu đối kháng,
 * Xem bảng xếp hạng, Cài đặt. Dev 1: đăng xuất, nút chơi với máy; Dev 2: nút đấu đối kháng (mở LobbyActivity,
 * chưa đăng nhập thì qua LoginActivity trước), nút xếp hạng, kết nối tới server khi hiện ra.
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

    /** Android 17 (API 37) trở lên: cần quyền mạng cục bộ mới kết nối được tới server trong mạng LAN. */
    private static boolean permissionAsked;
    private final ActivityResultLauncher<String> localNetworkPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    connect();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvPlayer = findViewById(R.id.tvPlayer);
        btnLogout = findViewById(R.id.btnLogout);
        btnPlayWithBot = findViewById(R.id.btnPlayWithBot);
        btnMatch = findViewById(R.id.btnMatch);
        btnRanking = findViewById(R.id.btnRanking);
        btnSettings = findViewById(R.id.btnSettings);
        ServerConnection.getInstance().setContext(this);
    }

    /**
     * Hiện tên người chơi (Khách nếu chưa đăng nhập) và kết nối tới server nếu chưa kết nối. Chạy ở onResume() để
     * cả khi quay lại từ một màn hình vừa đóng ngay (bảng xếp hạng khi chưa kết nối được) cũng thử kết nối lại.
     */
    @Override
    protected void onResume() {
        super.onResume();
        player = ClientSession.player;
        tvPlayer.setText(player == null ? getString(R.string.guest) : player.getUsername());
        btnLogout.setText(player == null ? R.string.login : R.string.logout);
        if (!ServerConnection.getInstance().isConnected()) {
            if (Build.VERSION.SDK_INT >= 37 && ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_LOCAL_NETWORK) != PackageManager.PERMISSION_GRANTED) {
                // hộp thoại xin quyền làm màn hình tạm dừng rồi chạy lại onResume(): chỉ hỏi một lần
                if (!permissionAsked) {
                    permissionAsked = true;
                    localNetworkPermission.launch(Manifest.permission.ACCESS_LOCAL_NETWORK);
                }
            } else {
                connect();
            }
        }
    }

    /** Gọi ServerConnection.connect() trên luồng nền với địa chỉ trong tài nguyên chuỗi. */
    private void connect() {
        String host = getString(R.string.server_host);
        int port = Integer.parseInt(getString(R.string.server_port));
        new Thread(() -> ServerConnection.getInstance().connect(host, port), "connect").start();
    }

    public void onLogoutClick(View v) {
        // TODO (Dev 1): khách thì mở LoginActivity; đã đăng nhập thì gửi LOGOUT, ClientSession.clear()
    }

    /** Mở RankingActivity; dùng được cả khi là khách. */
    public void onRankingClick(View v) {
        startActivity(new Intent(this, RankingActivity.class));
    }

    public void onPlayWithBotClick(View v) {
        // TODO (Dev 1)
    }

    public void onMatchClick(View v) {
        // TODO (Dev 2, bước sảnh)
    }

    @Override
    public void onMessage(Message msg) {
        super.onMessage(msg);
    }
}
