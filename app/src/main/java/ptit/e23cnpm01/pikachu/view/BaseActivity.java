package ptit.e23cnpm01.pikachu.view;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayDeque;
import ptit.e23cnpm01.pikachu.core.protocol.Message;
import ptit.e23cnpm01.pikachu.network.MessageListener;
import ptit.e23cnpm01.pikachu.network.ServerConnection;

/**
 * Lớp cha của các màn hình sau đăng nhập (trừ MatchActivity): nhận INVITE_RECEIVED, INVITE_CLOSED, MATCH_START ở mọi
 * màn hình; lời mời xếp hàng và hiện lần lượt bằng popup không chặn.
 *
 * <p>Màn hình đăng ký nhận message với ServerConnection trong onStart() và hủy trong onStop(). Lớp con xử lý các
 * message của riêng mình, message khác chuyển cho super.onMessage().
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        ServerConnection.getInstance().addListener(this);
    }

    @Override
    protected void onStop() {
        ServerConnection.getInstance().removeListener(this);
        super.onStop();
    }

    @Override
    public void onMessage(Message msg) {
        // TODO (bước thách đấu): INVITE_RECEIVED, INVITE_CLOSED, MATCH_START
    }

    /** Hiện lời mời kế tiếp còn hạn trong hàng đợi nếu chưa có popup nào. */
    public void showNextInvitation() {
        // TODO (bước thách đấu)
    }
}
