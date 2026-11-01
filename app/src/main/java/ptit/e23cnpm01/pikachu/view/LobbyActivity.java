package ptit.e23cnpm01.pikachu.view;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import ptit.e23cnpm01.pikachu.R;
import ptit.e23cnpm01.pikachu.core.model.Player;
import ptit.e23cnpm01.pikachu.core.model.PlayerRanking;
import ptit.e23cnpm01.pikachu.core.protocol.Message;
import ptit.e23cnpm01.pikachu.core.protocol.MessageType;
import ptit.e23cnpm01.pikachu.network.ClientSession;
import ptit.e23cnpm01.pikachu.network.ServerConnection;

/**
 * Màn hình đấu đối kháng: danh sách người chơi online, thách đấu. Khi mở thì gửi GET_ONLINE_LIST.
 *
 * <p>Sau đó danh sách cập nhật theo ONLINE_LIST mà server tự đẩy. Click người không bận thì mở
 * ChallengeConfirmDialog; INVITE_RECEIVED, INVITE_CLOSED, MATCH_START do lớp cha BaseActivity xử lý.
 *
 * <p>Phụ trách: Dev 2.
 */
public class LobbyActivity extends BaseActivity {
    /** Extra của Intent mở màn hình này: thông báo hiện trên tvMessage (bị xử thua do thoát, đối thủ đã dừng...). */
    public static final String EXTRA_MESSAGE = "message";

    private RecyclerView rvOnlinePlayers;
    private TextView tvMessage;
    private Button btnBack;
    private ArrayList<PlayerRanking> onlinePlayers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        rvOnlinePlayers = findViewById(R.id.rvOnlinePlayers);
        tvMessage = findViewById(R.id.tvMessage);
        btnBack = findViewById(R.id.btnBack);
        ViewGroup header = findViewById(R.id.headerRow);
        for (int i = 0; i < header.getChildCount(); i++) {
            ((TextView) header.getChildAt(i)).setTypeface(null, Typeface.BOLD_ITALIC);
        }
        Player me = ClientSession.player;
        ((TextView) findViewById(R.id.tvPlayer)).setText(me == null ? "" : me.getUsername());
        onlinePlayers = new ArrayList<>();
        rvOnlinePlayers.setLayoutManager(new LinearLayoutManager(this));
        rvOnlinePlayers.setAdapter(new OnlinePlayerAdapter());
        String message = getIntent().getStringExtra(EXTRA_MESSAGE);
        if (message != null) {
            tvMessage.setText(message);
        }

        if (!ServerConnection.getInstance().isConnected()) {
            Toast.makeText(this, R.string.cannot_connect, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        ServerConnection.getInstance().send(new Message(MessageType.GET_ONLINE_LIST));
    }

    /** Bỏ qua nếu người được chọn đang bận; ngược lại mở ChallengeConfirmDialog với người đó. */
    public void onPlayerClick(Player opponent) {
        if ("BUSY".equals(opponent.getStatus())) {
            return;
        }
        new ChallengeConfirmDialog(this, opponent).show();
    }

    /** Hiện thông báo (lý do lời mời không thành) trên tvMessage. */
    public void showMessage(String message) {
        tvMessage.setText(message);
    }

    public void onBackClick(View v) {
        finish();
    }

    /** ONLINE_LIST: cập nhật danh sách; các message khác chuyển cho BaseActivity. */
    @Override
    public void onMessage(Message msg) {
        if (!MessageType.ONLINE_LIST.equals(msg.getType())) {
            super.onMessage(msg);
            return;
        }
        ArrayList<PlayerRanking> list = msg.get("onlinePlayers",
                new TypeToken<ArrayList<PlayerRanking>>() { }.getType());
        onlinePlayers.clear();
        if (list != null) {
            onlinePlayers.addAll(list);
        }
        rvOnlinePlayers.getAdapter().notifyDataSetChanged();
    }

    /** Nhãn trạng thái trên giao diện: Rỗi, Luyện tập, Bận. */
    private String statusLabel(String status) {
        if ("BUSY".equals(status)) {
            return getString(R.string.status_busy);
        } else if ("TRAINING".equals(status)) {
            return getString(R.string.status_training);
        }
        return getString(R.string.status_idle);
    }

    /** Tổng điểm bỏ phần .0 (12.5, 9). */
    private static String formatPoint(float point) {
        return point == Math.rint(point) ? String.valueOf((int) point) : String.valueOf(point);
    }

    private class OnlinePlayerAdapter extends RecyclerView.Adapter<RowHolder> {
        @NonNull
        @Override
        public RowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RowHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_online_player, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RowHolder holder, int position) {
            PlayerRanking p = onlinePlayers.get(position);
            holder.tvIndex.setText(String.valueOf(position + 1));
            holder.tvName.setText(p.getUsername());
            holder.tvTotalPoint.setText(formatPoint(p.getTotalPoint()));
            holder.tvStatus.setText(statusLabel(p.getStatus()));
            holder.itemView.setOnClickListener(v -> onPlayerClick(p));
        }

        @Override
        public int getItemCount() {
            return onlinePlayers.size();
        }
    }

    private static class RowHolder extends RecyclerView.ViewHolder {
        final TextView tvIndex;
        final TextView tvName;
        final TextView tvTotalPoint;
        final TextView tvStatus;

        RowHolder(View itemView) {
            super(itemView);
            tvIndex = itemView.findViewById(R.id.tvIndex);
            tvName = itemView.findViewById(R.id.tvName);
            tvTotalPoint = itemView.findViewById(R.id.tvTotalPoint);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
