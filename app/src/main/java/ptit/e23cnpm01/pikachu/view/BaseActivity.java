package ptit.e23cnpm01.pikachu.view;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import ptit.e23cnpm01.pikachu.core.protocol.Message;
import ptit.e23cnpm01.pikachu.network.MessageListener;

/**
 * Lớp cha của các màn hình sau đăng nhập (trừ MatchActivity): nhận INVITE_RECEIVED, MATCH_START ở mọi màn hình.
 *
 * <p>Phụ trách: Dev 2.
 */
public abstract class BaseActivity extends AppCompatActivity implements MessageListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: setContentView, gắn view
    }

    @Override
    public void onMessage(Message msg) {
        // TODO
    }
}
