package ptit.e23cnpm01.pikachu.view;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatDialog;

/**
 * Dialog kết quả ván với máy.
 *
 * <p>Phụ trách: Dev 1.
 */
public class BotResultDialog extends AppCompatDialog {
    private TextView tvResult;
    private Button btnOk;

    public BotResultDialog(Context context) {
        super(context);
    }

    public void onOkClick(View v) {
        // TODO
    }
}
