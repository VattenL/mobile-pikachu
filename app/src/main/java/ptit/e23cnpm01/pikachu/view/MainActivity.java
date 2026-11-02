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
    private Button btnLogin;
    private Player player;
    private Button btnPlayWithBot;
    private Button btnMatch;
    private Button btnRanking;
    private Button btnSettings;

    /** Android 17 (API 37) trở lên: cần quyền mạng cục bộ mới kết nối được tới server trong mạng LAN. */
    /** Mở LoginActivity từ nút Đấu đối kháng; đăng nhập thành công thì vào màn hình đấu đối kháng. */
    private final ActivityResultLauncher<Intent> loginForMatch =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && ClientSession.player != null) {
                    startActivity(new Intent(this, LobbyActivity.class));
                }
            });

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
        btnLogin = findViewById(R.id.btnLogin);
        btnPlayWithBot = findViewById(R.id.btnPlayWithBot);
        btnMatch = findViewById(R.id.btnMatch);
        btnRanking = findViewById(R.id.btnRanking);
        btnSettings = findViewById(R.id.btnSettings);
        ServerConnection.getInstance().setContext(this);
    }

    /**
     * Cập nhật trạng thái người chơi và hiển thị nút Đăng nhập theo chế độ khách / người chơi.
     */
    private void updateUI() {
        player = ClientSession.player;
        if (player == null) {
            tvPlayer.setText(getString(R.string.guest));
            btnLogin.setVisibility(View.VISIBLE);
        } else {
            tvPlayer.setText(player.getUsername());
            btnLogin.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
        if (!ServerConnection.getInstance().isConnected()) {
            if (Build.VERSION.SDK_INT >= 37 && ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_LOCAL_NETWORK) != PackageManager.PERMISSION_GRANTED) {
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

    /** Mở LoginActivity khi người dùng khách bấm Đăng nhập */
    public void onLoginClick(View v) {
        startActivity(new Intent(this, LoginActivity.class));
    }

    /** Mở SettingDialog khi bấm Cài đặt; nếu người dùng chọn Đăng xuất trong dialog thì cập nhật lại UI */
    public void onSettingsClick(View v) {
        new SettingDialog(this, this::updateUI).show();
    }

    /** Mở RankingActivity; dùng được cả khi là khách. */
    public void onRankingClick(View v) {
        startActivity(new Intent(this, RankingActivity.class));
    }

    public void onPlayWithBotClick(View v) {
        // TODO (Dev 1)
    }

    /**
     * Chưa đăng nhập (khách) thì mở LoginActivity, đăng nhập xong (LoginActivity trả RESULT_OK) thì mở LobbyActivity;
     * đã đăng nhập thì mở thẳng LobbyActivity.
     */
    public void onMatchClick(View v) {
        if (ClientSession.player == null) {
            loginForMatch.launch(new Intent(this, LoginActivity.class));
        } else {
            startActivity(new Intent(this, LobbyActivity.class));
        }
    }

    @Override
    public void onMessage(Message msg) {
        super.onMessage(msg);
    }
}
