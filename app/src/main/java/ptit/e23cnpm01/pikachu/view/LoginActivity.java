package ptit.e23cnpm01.pikachu.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import ptit.e23cnpm01.pikachu.core.protocol.Message;
import ptit.e23cnpm01.pikachu.network.MessageListener;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: setContentView, gắn view
    }

    public void onLoginClick(View v) {
        // TODO
    }

    public void onRegisterClick(View v) {
        // TODO
    }

    @Override
    public void onMessage(Message msg) {
        // TODO
    }
}
