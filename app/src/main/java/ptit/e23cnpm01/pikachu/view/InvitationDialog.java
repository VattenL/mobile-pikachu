package ptit.e23cnpm01.pikachu.view;

import android.content.Context;
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
    private Button btnOk;
    private Button btnReject;
    private String challenger;

    public InvitationDialog(Context context) {
        super(context);
    }

    public void onOkClick(View v) {
        // TODO
    }

    public void onRejectClick(View v) {
        // TODO
    }
}
