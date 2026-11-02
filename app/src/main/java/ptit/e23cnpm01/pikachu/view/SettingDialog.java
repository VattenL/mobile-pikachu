package ptit.e23cnpm01.pikachu.view;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;
import ptit.e23cnpm01.pikachu.R;
import ptit.e23cnpm01.pikachu.core.protocol.Message;
import ptit.e23cnpm01.pikachu.core.protocol.MessageType;
import ptit.e23cnpm01.pikachu.network.ClientSession;
import ptit.e23cnpm01.pikachu.network.ServerConnection;
public class SettingDialog extends AppCompatDialog {
    /** Interface callback báo cho Activity khi người dùng thực hiện đăng xuất */
    public interface OnLogoutListener {
        void onLoggedOut();
    }
    private Button btnLogout;
    private Button btnClose;
    private final OnLogoutListener logoutListener;
    public SettingDialog(@NonNull Context context, OnLogoutListener listener) {
        super(context);
        this.logoutListener = listener;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_setting);
        btnLogout = findViewById(R.id.btnLogout);
        btnClose = findViewById(R.id.btnClose);
        // Nếu là khách (chưa đăng nhập), ẩn nút Đăng xuất trong cài đặt
        if (ClientSession.player == null) {
            btnLogout.setVisibility(View.GONE);
        } else {
            btnLogout.setVisibility(View.VISIBLE);
            btnLogout.setOnClickListener(this::onLogoutClick);
        }
        btnClose.setOnClickListener(v -> dismiss());
    }
    /**
     * Xử lý đăng xuất:
     * 1. Gửi bản tin LOGOUT tới server nếu đang kết nối.
     * 2. Xóa thông tin phiên người chơi hiện tại trong ClientSession.
     * 3. Đóng dialog và gọi callback để MainActivity cập nhật lại giao diện.
     */
    private void onLogoutClick(View v) {
        if (ServerConnection.getInstance().isConnected()) {
            ServerConnection.getInstance().send(new Message(MessageType.LOGOUT));
        }
        ClientSession.clear();
        dismiss();
        if (logoutListener != null) {
            logoutListener.onLoggedOut();
        }
    }
}
