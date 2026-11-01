package ptit.e23cnpm01.pikachu.view;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatDialog;
import ptit.e23cnpm01.pikachu.R;
import ptit.e23cnpm01.pikachu.core.protocol.Message;
import ptit.e23cnpm01.pikachu.core.protocol.MessageType;
import ptit.e23cnpm01.pikachu.network.ServerConnection;

/**
 * Dialog lời mời trên máy người được mời.
 *
 * <p>Popup không chặn (cửa sổ không modal) ở mép trên màn hình: màn hình bên dưới vẫn nhận chạm và phím, nên ván với
 * máy vẫn chạy. Đếm ngược tới hạn của lời mời (tính từ timeoutSec của INVITE_RECEIVED); Accept, Reject thì gửi
 * INVITE_REPLY rồi đóng, hết giờ thì chỉ đóng (server tự hủy lời mời). Khi đóng, BaseActivity hiện lời mời kế tiếp.
 *
 * <p>Phụ trách: Dev 2.
 */
public class InvitationDialog extends AppCompatDialog {
    private TextView tvChallenger;
    private Button btnAccept;
    private Button btnReject;
    private TextView tvCountdown;
    private String challenger;
    private CountDownTimer countDown;
    /** Message INVITE_RECEIVED của lời mời này, kèm trường deadline (giờ hết hạn trên máy) do BaseActivity thêm. */
    private final Message invitation;

    public InvitationDialog(BaseActivity owner, Message invitation) {
        super(owner);
        setOwnerActivity(owner);
        this.invitation = invitation;
        this.challenger = invitation.getString("from");
    }

    public Message getInvitation() {
        return invitation;
    }

    public String getChallenger() {
        return challenger;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_invitation);
        Window window = getWindow();
        if (window != null) {
            window.setGravity(Gravity.TOP);
            window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
        setCancelable(false);
        tvChallenger = findViewById(R.id.tvChallenger);
        tvCountdown = findViewById(R.id.tvCountdown);
        btnAccept = findViewById(R.id.btnAccept);
        btnReject = findViewById(R.id.btnReject);
        btnAccept.setOnClickListener(this::onAcceptClick);
        btnReject.setOnClickListener(this::onRejectClick);
        tvChallenger.setText(getContext().getString(R.string.invitation_challenger, challenger));
        BaseActivity owner = (BaseActivity) getOwnerActivity();
        setOnDismissListener(d -> {
            if (countDown != null) {
                countDown.cancel();
            }
            owner.onInvitationClosed(this);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        long remaining = invitation.get("deadline", Long.class) - System.currentTimeMillis();
        countDown = new CountDownTimer(Math.max(remaining, 0), 250) {
            @Override
            public void onTick(long millisUntilFinished) {
                showRemaining(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                dismiss();
            }
        };
        showRemaining(remaining);
        countDown.start();
    }

    private void showRemaining(long millis) {
        int seconds = (int) Math.max(0, (millis + 999) / 1000);
        tvCountdown.setText(getContext().getString(R.string.invitation_countdown, seconds));
    }

    public void onAcceptClick(View v) {
        reply(true);
    }

    public void onRejectClick(View v) {
        reply(false);
    }

    private void reply(boolean accept) {
        ServerConnection.getInstance().send(new Message(MessageType.INVITE_REPLY)
                .put("from", challenger).put("accept", accept));
        dismiss();
    }
}
