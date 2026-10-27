package ptit.e23cnpm01.pikachu.view;

import android.content.Context;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatDialog;
import ptit.e23cnpm01.pikachu.core.model.Player;
import ptit.e23cnpm01.pikachu.core.protocol.Message;
import ptit.e23cnpm01.pikachu.network.MessageListener;

/**
 * Dialog xác nhận thách đấu; sau khi xác nhận thành popup chờ (đếm ngược 30 giây, nút Hủy lời mời).
 *
 * <p>Phụ trách: Dev 2.
 */
public class ChallengeConfirmDialog extends AppCompatDialog implements MessageListener {
    private TextView tvMessage;
    private Button btnConfirm;
    private Button btnCancel;
    private Player opponent;
    private CountDownTimer countDown;

    public ChallengeConfirmDialog(Context context) {
        super(context);
    }

    public void onConfirmClick(View v) {
        // TODO
    }

    public void onCancelClick(View v) {
        // TODO
    }

    @Override
    public void onMessage(Message msg) {
        // TODO
    }
}
