package ptit.e23cnpm01.pikachu.view;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatDialog;
import ptit.e23cnpm01.pikachu.core.protocol.Message;
import ptit.e23cnpm01.pikachu.network.MessageListener;

/**
 * Dialog kết quả ván đối kháng, hỏi tiếp tục hay dừng.
 *
 * <p>Phụ trách: Dev 2.
 */
public class ResultDialog extends AppCompatDialog implements MessageListener {
    private TextView tvResult;
    private Button btnContinue;
    private Button btnStop;
    private int matchId;

    public ResultDialog(Context context) {
        super(context);
    }

    public void onContinueClick(View v) {
        // TODO
    }

    public void onStopClick(View v) {
        // TODO
    }

    @Override
    public void onMessage(Message msg) {
        // TODO
    }
}
