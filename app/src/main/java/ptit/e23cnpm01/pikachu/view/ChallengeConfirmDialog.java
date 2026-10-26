package ptit.e23cnpm01.pikachu.view;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatDialog;
import ptit.e23cnpm01.pikachu.core.model.Player;

/**
 * Dialog xác nhận thách đấu.
 *
 * <p>Phụ trách: Dev 2.
 */
public class ChallengeConfirmDialog extends AppCompatDialog {
    private TextView tvOpponent;
    private Button btnConfirm;
    private Button btnCancel;
    private Player opponent;

    public ChallengeConfirmDialog(Context context) {
        super(context);
    }

    public void onConfirmClick(View v) {
        // TODO
    }

    public void onCancelClick(View v) {
        // TODO
    }
}
