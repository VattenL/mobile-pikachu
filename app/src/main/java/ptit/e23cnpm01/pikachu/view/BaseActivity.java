package ptit.e23cnpm01.pikachu.view;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import java.util.ArrayDeque;
import java.util.Iterator;
import ptit.e23cnpm01.pikachu.core.protocol.Message;
import ptit.e23cnpm01.pikachu.core.protocol.MessageType;
import ptit.e23cnpm01.pikachu.network.MessageListener;
import ptit.e23cnpm01.pikachu.network.ServerConnection;

/**
 * Lớp cha của các màn hình sau đăng nhập (trừ MatchActivity): nhận INVITE_RECEIVED, INVITE_CLOSED, MATCH_START ở mọi
 * màn hình; lời mời xếp hàng và hiện lần lượt bằng popup không chặn.
 *
 * <p>Màn hình đăng ký nhận message với ServerConnection trong onStart() và hủy trong onStop(). Lớp con xử lý các
 * message của riêng mình, message khác chuyển cho super.onMessage(). Chỉ màn hình đang ở trên cùng (resumed) xử lý
 * ba message chung, vì lúc chuyển màn hình có hai màn hình cùng đăng ký trong chốc lát.
 *
 * <p>Phụ trách: Dev 2.
 */
public abstract class BaseActivity extends AppCompatActivity implements MessageListener {
    /** Extra của Intent mở MatchActivity: message MATCH_START dạng JSON (Message.toJson()). */
    public static final String EXTRA_MATCH_START = "matchStart";

    /** Hàng đợi lời mời, dùng chung cho mọi màn hình. */
    private static ArrayDeque<Message> invitationQueue = new ArrayDeque<>();
    /** Popup lời mời đang hiện, rỗng nếu không có. */
    private static InvitationDialog invitationDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Đăng ký nhận message. Popup lời mời đang hiện trên màn hình trước (vừa chuyển màn hình) thì chuyển sang màn hình
     * này; không có thì hiện lời mời kế tiếp trong hàng đợi.
     */
    @Override
    protected void onStart() {
        super.onStart();
        ServerConnection.getInstance().addListener(this);
        if (invitationDialog != null && invitationDialog.getOwnerActivity() != this) {
            InvitationDialog old = invitationDialog;
            invitationQueue.addFirst(old.getInvitation());
            invitationDialog = null;
            old.dismiss();
        }
        showNextInvitation();
    }

    /** Hủy đăng ký. Popup lời mời còn trên màn hình này (không có màn hình nào nhận thay) thì cất lại vào hàng đợi. */
    @Override
    protected void onStop() {
        ServerConnection.getInstance().removeListener(this);
        if (invitationDialog != null && invitationDialog.getOwnerActivity() == this) {
            InvitationDialog old = invitationDialog;
            invitationQueue.addFirst(old.getInvitation());
            invitationDialog = null;
            old.dismiss();
        }
        super.onStop();
    }

    /**
     * INVITE_RECEIVED: thêm vào hàng đợi kèm hạn (lúc nhận + timeoutSec) rồi hiện nếu chưa có popup nào.
     * INVITE_CLOSED: bỏ lời mời của from khỏi hàng đợi, đóng popup nếu đang hiện lời mời đó.
     * MATCH_START: đóng popup, xóa hàng đợi, mở MatchActivity, đóng màn hình hiện tại (trừ màn hình gốc).
     */
    @Override
    public void onMessage(Message msg) {
        if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            return;
        }
        String type = msg.getType();
        if (MessageType.INVITE_RECEIVED.equals(type)) {
            msg.put("deadline", System.currentTimeMillis() + msg.getInt("timeoutSec") * 1000L);
            invitationQueue.addLast(msg);
            showNextInvitation();
        } else if (MessageType.INVITE_CLOSED.equals(type)) {
            String from = msg.getString("from");
            Iterator<Message> it = invitationQueue.iterator();
            while (it.hasNext()) {
                if (it.next().getString("from").equals(from)) {
                    it.remove();
                }
            }
            if (invitationDialog != null && invitationDialog.getChallenger().equals(from)) {
                invitationDialog.dismiss();
            }
        } else if (MessageType.MATCH_START.equals(type)) {
            invitationQueue.clear();
            if (invitationDialog != null) {
                InvitationDialog old = invitationDialog;
                invitationDialog = null;
                old.dismiss();
            }
            startActivity(new Intent(this, MatchActivity.class).putExtra(EXTRA_MATCH_START, msg.toJson()));
            if (!isTaskRoot()) {
                finish();
            }
        }
    }

    /** Hiện lời mời kế tiếp còn hạn trong hàng đợi nếu chưa có popup nào; lời mời quá hạn bị bỏ. */
    public void showNextInvitation() {
        if (invitationDialog != null || isFinishing()) {
            return;
        }
        while (!invitationQueue.isEmpty()) {
            Message next = invitationQueue.pollFirst();
            if (next.get("deadline", Long.class) > System.currentTimeMillis()) {
                invitationDialog = new InvitationDialog(this, next);
                invitationDialog.show();
                return;
            }
        }
    }

    /** Popup lời mời vừa đóng (trả lời, hết giờ, bị đóng): hiện lời mời kế tiếp. */
    void onInvitationClosed(InvitationDialog dialog) {
        if (invitationDialog == dialog) {
            invitationDialog = null;
            showNextInvitation();
        }
    }
}
