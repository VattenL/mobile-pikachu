package ptit.e23cnpm01.pikachu.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import ptit.e23cnpm01.pikachu.R;
import ptit.e23cnpm01.pikachu.core.protocol.Message;
import ptit.e23cnpm01.pikachu.core.protocol.MessageType;
import ptit.e23cnpm01.pikachu.network.ServerConnection;
import ptit.e23cnpm01.pikachu.network.MessageListener;

/**
 * Giao diện đăng ký.
 *
 * <p>Phụ trách: Dev 1.
 */
public class RegisterActivity extends AppCompatActivity implements MessageListener {
    private EditText edtUsername;
    private EditText edtPassword;
    private EditText edtConfirmPassword;
    private EditText edtName;
    private TextView tvError;
    private Button btnRegister;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        edtName = findViewById(R.id.edtName);
        tvError = findViewById(R.id.tvError);
        btnRegister = findViewById(R.id.btnRegister);
        btnBack = findViewById(R.id.btnBack);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ServerConnection.getInstance().addListener(this);
    }
    @Override
    protected void onStop() {
        super.onStop();
        ServerConnection.getInstance().removeListener(this);
    }

    public void onRegisterClick(View v) {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();
        String name = edtName.getText().toString().trim();
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || name.isEmpty()) {
            tvError.setTextColor(0xFFD32F2F);
            tvError.setText("Vui lòng điền đầy đủ các trường");
            return;
        }
        if (!password.equals(confirmPassword)) {
            tvError.setTextColor(0xFFD32F2F);
            tvError.setText("Mật khẩu nhập lại không khớp");
            return;
        }
        if (!ServerConnection.getInstance().isConnected()) {
            tvError.setTextColor(0xFFD32F2F);
            tvError.setText("Không có kết nối tới máy chủ");
            return;
        }
        tvError.setText("");
        Message msg = new Message(MessageType.REGISTER);
        msg.put("username", username);
        msg.put("password", password);
        msg.put("name", name);
        ServerConnection.getInstance().send(msg);
    }

    public void onBackClick(View v) {
        finish();
    }
    
    @Override
    public void onMessage(Message msg) {
        if (MessageType.REGISTER_RESULT.equals(msg.getType())) {
            boolean ok = Boolean.TRUE.equals(msg.get("ok", Boolean.class));
            if (ok) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("registered_username", edtUsername.getText().toString().trim());
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                String error = msg.get("error", String.class);
                tvError.setTextColor(0xFFD32F2F);
                tvError.setText(error != null ? error : "Đăng ký thất bại");
                edtPassword.setText("");
                edtConfirmPassword.setText("");
            }
        }
    }
}
