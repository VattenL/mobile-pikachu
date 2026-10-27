package ptit.e23cnpm01.pikachu.view;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayDeque;
import ptit.e23cnpm01.pikachu.core.protocol.Message;
import ptit.e23cnpm01.pikachu.network.MessageListener;

/**
 * Lớp cha của các màn hình sau đăng nhập (trừ MatchActivity): nhận INVITE_RECEIVED, INVITE_CLOSED, MATCH_START ở mọi
 * màn hình; lời mời xếp hàng và hiện lần lượt bằng popup không chặn.
 *
 * <p>Phụ trách: Dev 2.
 */
public abstract class BaseActivity extends AppCompatActivity implements MessageListener {
    /** Hàng đợi lời mời, dùng chung cho mọi màn hình. */
    private static ArrayDeque<Message> invitationQueue = new ArrayDeque<>();
    /** Popup lời mời đang hiện, rỗng nếu không có. */
    private static InvitationDialog invitationDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: setContentView, gắn view
    }

    @Override
    public void onMessage(Message msg) {
        // TODO
    }

    /** Hiện lời mời kế tiếp còn hạn trong hàng đợi nếu chưa có popup nào. */
    public void showNextInvitation() {
        // TODO
    }
}
