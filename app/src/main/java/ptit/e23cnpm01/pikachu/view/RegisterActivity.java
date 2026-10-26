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
        // TODO: setContentView, gắn view
    }

    public void onRegisterClick(View v) {
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
