package ptit.e23cnpm01.pikachu.view;

import android.content.Context;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatDialog;

/**
 * Dialog lời mời trên máy người được mời.
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

    public InvitationDialog(Context context) {
        super(context);
    }

    public void onAcceptClick(View v) {
        // TODO
    }

    public void onRejectClick(View v) {
        // TODO
    }
}
