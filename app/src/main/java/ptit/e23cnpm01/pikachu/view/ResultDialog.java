package ptit.e23cnpm01.pikachu.view;

import android.content.Context;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatDialog;
import ptit.e23cnpm01.pikachu.core.protocol.Message;
import ptit.e23cnpm01.pikachu.network.MessageListener;

/**
 * Dialog kết quả ván đối kháng, hỏi tái đấu hay dừng.
 *
 * <p>Phụ trách: Dev 2.
 */
public class ResultDialog extends AppCompatDialog implements MessageListener {
    private TextView tvResult;
    private Button btnRematch;
    private Button btnStop;
    private TextView tvCountdown;
    private int roomId;
    private CountDownTimer countDown;

    public ResultDialog(Context context) {
        super(context);
    }

    public void onRematchClick(View v) {
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
