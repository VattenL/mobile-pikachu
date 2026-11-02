package ptit.e23cnpm01.pikachu.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import ptit.e23cnpm01.pikachu.network.MessageListener;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import ptit.e23cnpm01.pikachu.R;
import ptit.e23cnpm01.pikachu.core.model.Player;
import ptit.e23cnpm01.pikachu.core.protocol.Message;
import ptit.e23cnpm01.pikachu.core.protocol.MessageType;
import ptit.e23cnpm01.pikachu.network.ClientSession;
import ptit.e23cnpm01.pikachu.network.ServerConnection;

/**
 * Giao diện đăng nhập.
 *
 * <p>Phụ trách: Dev 1.
 */
public class LoginActivity extends AppCompatActivity implements MessageListener {
    private EditText edtUsername;
    private EditText edtPassword;
    private TextView tvError;
    private Button btnLogin;
    private Button btnRegister;

    private final ActivityResultLauncher<Intent> registerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String registeredUser = result.getData().getStringExtra("registered_username");
                    if (registeredUser != null && !registeredUser.isEmpty()) {
                        edtUsername.setText(registeredUser);
                        edtPassword.setText("");
                        tvError.setTextColor(0xFF2E7D32);
                        tvError.setText("Đăng ký thành công, mời đăng nhập");
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        tvError = findViewById(R.id.tvError);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Đăng ký nhận bản tin từ server khi màn hình hiển thị
        ServerConnection.getInstance().addListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Hủy đăng ký khi rời khỏi màn hình
        ServerConnection.getInstance().removeListener(this);
    }

    public void onLoginClick(View v) {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        if (username.isEmpty() || password.isEmpty()) {
            tvError.setTextColor(0xFFD32F2F); // Màu đỏ báo lỗi
            tvError.setText("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu");
            return;
        }
        if (!ServerConnection.getInstance().isConnected()) {
            tvError.setTextColor(0xFFD32F2F);
            tvError.setText("Không có kết nối tới máy chủ");
            return;
        }
        tvError.setText("");
        Message msg = new Message(MessageType.LOGIN);
        msg.put("username", username);
        msg.put("password", password);
        ServerConnection.getInstance().send(msg);
    }

    public void onRegisterClick(View v) {
        Intent intent = new Intent(this, RegisterActivity.class);
        registerLauncher.launch(intent);
    }

    @Override
    public void onMessage(Message msg) {
        if (MessageType.LOGIN_RESULT.equals(msg.getType())) {
            boolean ok = Boolean.TRUE.equals(msg.get("ok", Boolean.class));
            if (ok) {
                Player player = msg.get("player", Player.class);
                ClientSession.player = player;
                setResult(RESULT_OK);
                finish();
            } else {
                String error = msg.get("error", String.class);
                tvError.setTextColor(0xFFD32F2F);
                tvError.setText(error != null ? error : "Đăng nhập thất bại");
            }
        }
    }
}
