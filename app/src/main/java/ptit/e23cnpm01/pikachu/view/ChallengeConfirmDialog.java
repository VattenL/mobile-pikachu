package ptit.e23cnpm01.pikachu.view;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatDialog;
import ptit.e23cnpm01.pikachu.R;
import ptit.e23cnpm01.pikachu.core.model.Player;
import ptit.e23cnpm01.pikachu.core.protocol.Message;
import ptit.e23cnpm01.pikachu.core.protocol.MessageType;
import ptit.e23cnpm01.pikachu.network.MessageListener;
import ptit.e23cnpm01.pikachu.network.ServerConnection;

/**
 * Dialog xác nhận thách đấu; sau khi xác nhận thành popup chờ (đếm ngược 30 giây, nút Hủy lời mời).
 *
 * <p>Không đóng được khi chạm ra ngoài hay bấm Back, nên mỗi lúc một người chỉ có một lời mời đi. Nhận INVITE_RESULT
 * thì đóng và hiện lý do trên LobbyActivity; nhận MATCH_START thì đóng (BaseActivity mở ván).
 *
 * <p>Phụ trách: Dev 2.
 */
public class ChallengeConfirmDialog extends AppCompatDialog implements MessageListener {
    private static final long INVITE_TIMEOUT_MS = 30000;

    private TextView tvMessage;
    private Button btnConfirm;
    private Button btnCancel;
    private Player opponent;
    private CountDownTimer countDown;
    private final LobbyActivity lobby;

    public ChallengeConfirmDialog(LobbyActivity lobby, Player opponent) {
        super(lobby);
        this.lobby = lobby;
        this.opponent = opponent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_challenge_confirm);
        setCancelable(false);
        tvMessage = findViewById(R.id.tvMessage);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnCancel = findViewById(R.id.btnCancel);
        btnConfirm.setOnClickListener(this::onConfirmClick);
        btnCancel.setOnClickListener(this::onCancelClick);
        tvMessage.setText(getContext().getString(R.string.confirm_message, opponent.getUsername()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        ServerConnection.getInstance().addListener(this);
    }

    @Override
    protected void onStop() {
        ServerConnection.getInstance().removeListener(this);
        if (countDown != null) {
            countDown.cancel();
        }
        super.onStop();
    }

    /** Gửi INVITE rồi chuyển thành popup chờ "Đang chờ ... trả lời" đếm ngược 30 giây, nút Hủy thành Hủy lời mời. */
    public void onConfirmClick(View v) {
        ServerConnection.getInstance().send(new Message(MessageType.INVITE).put("to", opponent.getUsername()));
        ((TextView) findViewById(R.id.tvTitle)).setText(R.string.waiting_title);
        btnConfirm.setVisibility(View.GONE);
        btnCancel.setText(R.string.cancel_invitation);
        countDown = new CountDownTimer(INVITE_TIMEOUT_MS, 250) {
            @Override
            public void onTick(long millisUntilFinished) {
                showWaiting(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                showWaiting(0);
            }
        };
        showWaiting(INVITE_TIMEOUT_MS);
        countDown.start();
    }

    private void showWaiting(long millis) {
        int seconds = (int) ((millis + 999) / 1000);
        tvMessage.setText(getContext().getString(R.string.waiting_message, opponent.getUsername(), seconds));
    }

    /** Trước khi xác nhận: chỉ đóng dialog. Khi đang chờ: gửi INVITE_CANCEL rồi đóng. */
    public void onCancelClick(View v) {
        if (countDown != null) {
            ServerConnection.getInstance().send(new Message(MessageType.INVITE_CANCEL)
                    .put("to", opponent.getUsername()));
        }
        dismiss();
    }

    @Override
    public void onMessage(Message msg) {
        if (MessageType.INVITE_RESULT.equals(msg.getType())
                && opponent.getUsername().equals(msg.getString("to"))) {
            dismiss();
            lobby.showMessage(reasonText(msg.getString("reason")));
        } else if (MessageType.MATCH_START.equals(msg.getType())) {
            dismiss();
        }
    }

    /** Câu báo theo lý do của INVITE_RESULT, như kịch bản pha 2 (ngoại lệ 6a, 7b, 7c, 7d, 7e). */
    private String reasonText(String reason) {
        String name = opponent.getUsername();
        if ("REJECTED".equals(reason)) {
            return getContext().getString(R.string.invite_rejected, name);
        } else if ("BUSY".equals(reason)) {
            return getContext().getString(R.string.invite_busy, name);
        } else if ("OFFLINE".equals(reason)) {
            return getContext().getString(R.string.invite_offline, name);
        }
        return getContext().getString(R.string.invite_expired, name);
    }
}
