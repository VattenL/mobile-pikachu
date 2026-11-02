package ptit.e23cnpm01.pikachu.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatDialog;
import java.util.Locale;
import ptit.e23cnpm01.pikachu.R;
import ptit.e23cnpm01.pikachu.core.protocol.Message;
import ptit.e23cnpm01.pikachu.core.protocol.MessageType;
import ptit.e23cnpm01.pikachu.network.MessageListener;
import ptit.e23cnpm01.pikachu.network.ServerConnection;

/**
 * Dialog kết quả ván đối kháng, hỏi tái đấu hay dừng (Hình 3.14).
 *
 * <p>MatchActivity mở dialog khi nhận MATCH_END: {@code new ResultDialog(this, msg, opponent.getUsername()).show()}.
 * Dialog hiện kết quả, điểm, thời gian hoàn thành hoặc số ô còn lại và đếm ngược replySec giây. MATCH_END có rematch =
 * false (đối thủ thoát hoặc mất kết nối) thì ẩn nút Tái đấu, nút Dừng mang chữ Về sảnh. Dialog tự nhận MATCH_START
 * (cả hai đồng ý: mở MatchActivity với ván mới) và OPPONENT_LEFT (về sảnh, báo đối thủ đã dừng chơi), nên
 * MatchActivity bỏ qua hai message này. Không đóng được bằng Back hay chạm ra ngoài.
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
    private final MatchActivity matchActivity;
    /** Message MATCH_END của ván vừa kết thúc. */
    private final Message matchEnd;
    /** Tên đăng nhập của đối thủ, để báo "B đã dừng chơi". */
    private final String opponent;
    /** Đã bấm Tái đấu, đang chờ đối thủ trả lời. */
    private boolean waiting;
    /** Đã rời dialog (về sảnh hoặc sang ván mới): bỏ qua mọi thao tác sau đó. */
    private boolean closed;

    public ResultDialog(MatchActivity matchActivity, Message matchEnd, String opponent) {
        super(matchActivity);
        setOwnerActivity(matchActivity);
        this.matchActivity = matchActivity;
        this.matchEnd = matchEnd;
        this.opponent = opponent;
        this.roomId = matchEnd.getInt("roomId");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_result);
        setCancelable(false);
        tvResult = findViewById(R.id.tvResult);
        tvCountdown = findViewById(R.id.tvCountdown);
        btnRematch = findViewById(R.id.btnRematch);
        btnStop = findViewById(R.id.btnStop);
        btnRematch.setOnClickListener(this::onRematchClick);
        btnStop.setOnClickListener(this::onStopClick);

        String result = matchEnd.getString("result");
        TextView tvTitle = findViewById(R.id.tvTitle);
        if ("WIN".equals(result)) {
            tvTitle.setText(R.string.result_title_win);
        } else if ("DRAW".equals(result)) {
            tvTitle.setText(R.string.result_title_draw);
        } else {
            tvTitle.setText(R.string.result_title_lose);
        }
        tvResult.setText(resultText());
        if (!matchEnd.getBoolean("rematch")) {
            btnRematch.setVisibility(View.GONE);
            btnStop.setText(R.string.back_to_lobby);
        }

        long replyMs = matchEnd.getInt("replySec") * 1000L;
        countDown = new CountDownTimer(replyMs, 250) {
            @Override
            public void onTick(long millisUntilFinished) {
                showCountdown(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                showCountdown(0);
                onStopClick(null);
            }
        };
        showCountdown(replyMs);
        countDown.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        ServerConnection.getInstance().addListener(this);
    }

    @Override
    protected void onStop() {
        ServerConnection.getInstance().removeListener(this);
        if (countDown != null) {
            countDown.cancel();
        }
        super.onStop();
    }

    /**
     * "Bạn thắng (+1 điểm). Thời gian hoàn thành 06:42.", "Bạn thắng (+1 điểm). Đối thủ đã thoát.", "Bạn thua (+0
     * điểm). Bạn còn 18 ô.", "Hòa (+0.5 điểm). Bạn còn 40 ô." như kịch bản pha 2.
     */
    private String resultText() {
        String result = matchEnd.getString("result");
        if ("WIN".equals(result)) {
            if (matchEnd.has("finishTime")) {
                return getContext().getString(R.string.result_win_time, formatTime(matchEnd.getInt("finishTime")));
            }
            return getContext().getString(R.string.result_win_quit);
        }
        int remaining = matchEnd.getInt("remaining");
        if ("DRAW".equals(result)) {
            return getContext().getString(R.string.result_draw, remaining);
        }
        return getContext().getString(R.string.result_lose, remaining);
    }

    /** Mili giây thành mm:ss, làm tròn xuống tới giây. */
    private static String formatTime(int millis) {
        int seconds = millis / 1000;
        return String.format(Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60);
    }

    private void showCountdown(long millis) {
        int seconds = (int) ((millis + 999) / 1000);
        if (waiting) {
            tvCountdown.setText(getContext().getString(R.string.rematch_waiting, seconds));
        } else if (matchEnd.getBoolean("rematch")) {
            tvCountdown.setText(getContext().getString(R.string.rematch_question, seconds));
        } else {
            tvCountdown.setText(getContext().getString(R.string.reply_countdown, seconds));
        }
    }

    /** Gửi REMATCH_REPLY đồng ý, hiện "Đang chờ đối thủ trả lời..."; nút Dừng vẫn bấm được. */
    public void onRematchClick(View v) {
        if (closed || waiting) {
            return;
        }
        ServerConnection.getInstance().send(new Message(MessageType.REMATCH_REPLY)
                .put("roomId", roomId).put("accept", true));
        waiting = true;
        btnRematch.setEnabled(false);
    }

    /** Dừng, Về sảnh hoặc hết giờ trả lời: gửi REMATCH_REPLY từ chối rồi về LobbyActivity. */
    public void onStopClick(View v) {
        if (closed) {
            return;
        }
        ServerConnection.getInstance().send(new Message(MessageType.REMATCH_REPLY)
                .put("roomId", roomId).put("accept", false));
        goToLobby(null);
    }

    /**
     * MATCH_START (cả hai đồng ý tái đấu): đóng dialog và màn chơi cũ, mở MatchActivity với ván mới. OPPONENT_LEFT:
     * về LobbyActivity kèm thông báo đối thủ đã dừng chơi.
     */
    @Override
    public void onMessage(Message msg) {
        if (closed) {
            return;
        }
        if (MessageType.MATCH_START.equals(msg.getType())) {
            closed = true;
            dismiss();
            matchActivity.startActivity(new Intent(matchActivity, MatchActivity.class)
                    .putExtra(BaseActivity.EXTRA_MATCH_START, msg.toJson()));
            matchActivity.finish();
        } else if (MessageType.OPPONENT_LEFT.equals(msg.getType())) {
            goToLobby(getContext().getString(R.string.opponent_stopped, opponent));
        }
    }

    /** Đóng dialog và màn chơi, mở LobbyActivity (kèm thông báo nếu có). */
    private void goToLobby(String message) {
        closed = true;
        dismiss();
        Intent intent = new Intent(matchActivity, LobbyActivity.class);
        if (message != null) {
            intent.putExtra(LobbyActivity.EXTRA_MESSAGE, message);
        }
        matchActivity.startActivity(intent);
        matchActivity.finish();
    }
}
