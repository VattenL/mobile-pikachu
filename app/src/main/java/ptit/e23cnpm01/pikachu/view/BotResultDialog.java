package ptit.e23cnpm01.pikachu.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatDialog;
import ptit.e23cnpm01.pikachu.R;

/**
 * Hộp thoại hiển thị kết quả ván chơi với máy (Hình 4.4, Hình 3.8, Hình 3.20).
 *
 * <p>Luồng thực thi:
 * 1. BotGameActivity gọi showResultDialog(result) và thiết lập kết quả ("WIN", "LOSE", "DRAW").
 * 2. Dialog hiển thị tiêu đề và thông điệp kết quả tương ứng.
 * 3. Khi người chơi bấm nút "OK" (onOkClick), đóng dialog và quay trở lại MainActivity.
 *
 * <p>Phụ trách: Dev 1.
 */
public class BotResultDialog extends AppCompatDialog {
    private TextView tvTitle;
    private TextView tvResult;
    private Button btnOk;
    private String result;

    public BotResultDialog(Context context) {
        super(context);
        if (context instanceof Activity) {
            setOwnerActivity((Activity) context);
        }
    }

    public BotResultDialog(Context context, String result) {
        this(context);
        this.result = result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_bot_result);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        tvTitle = findViewById(R.id.tvTitle);
        tvResult = findViewById(R.id.tvResult);
        btnOk = findViewById(R.id.btnOk);

        if (btnOk != null) {
            btnOk.setOnClickListener(this::onOkClick);
        }

        updateResultUI();
    }

    /**
     * Cập nhật chuỗi kết quả ván đấu:
     * - "WIN": Người chơi hoàn thành trước máy.
     * - "LOSE": Máy hoàn thành trước người chơi.
     * - "DRAW": Hết thời gian 10 phút.
     * Có thể gọi trước hoặc sau khi dialog được hiển thị (show()).
     */
    public void setResult(String result) {
        this.result = result;
        updateResultUI();
    }

    private void updateResultUI() {
        if (tvResult == null) {
            return;
        }

        if ("WIN".equalsIgnoreCase(result)) {
            if (tvTitle != null) {
                tvTitle.setText(R.string.result_title_win);
            }
            tvResult.setText("Chiến thắng! Bạn đã hoàn thành trước máy.");
        } else if ("LOSE".equalsIgnoreCase(result)) {
            if (tvTitle != null) {
                tvTitle.setText(R.string.result_title_lose);
            }
            tvResult.setText("Thất bại! Máy đã hoàn thành trước bạn.");
        } else {
            if (tvTitle != null) {
                tvTitle.setText(R.string.result_title_draw);
            }
            tvResult.setText("Hòa cờ! Đã hết 10 phút thi đấu.");
        }
    }

    /**
     * Xử lý khi người chơi bấm nút "OK" (Bước 50 - 51 Biểu đồ tuần tự Hình 3.20):
     * 1. Đóng hộp thoại kết quả.
     * 2. Kết thúc BotGameActivity và điều hướng về MainActivity.
     */
    public void onOkClick(View v) {
        dismiss();
        Activity activity = getOwnerActivity();
        if (activity == null && getContext() instanceof Activity) {
            activity = (Activity) getContext();
        }
        if (activity != null) {
            Intent intent = new Intent(activity, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activity.startActivity(intent);
            activity.finish();
        }
    }
}