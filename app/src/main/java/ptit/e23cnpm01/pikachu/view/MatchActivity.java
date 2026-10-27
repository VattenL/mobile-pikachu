package ptit.e23cnpm01.pikachu.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import ptit.e23cnpm01.pikachu.core.model.Board;
import ptit.e23cnpm01.pikachu.core.model.Cell;
import ptit.e23cnpm01.pikachu.core.model.Player;
import ptit.e23cnpm01.pikachu.core.protocol.Message;
import ptit.e23cnpm01.pikachu.network.MessageListener;

/**
 * Giao diện đấu đối kháng (không kế thừa BaseActivity: người đang đấu không nhận lời mời).
 *
 * <p>Phụ trách: Dev 1.
 */
public class MatchActivity extends AppCompatActivity implements MessageListener {
    private BoardView boardView;
    private BoardView opponentBoardView;
    private TextView tvTimer;
    private TextView tvMyRemaining;
    private TextView tvOpponentRemaining;
    private Button btnQuit;
    private Board board;
    private Board opponentBoard;
    private int roomId;
    private Player opponent;
    private Cell firstCell;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: setContentView, gắn view
    }

    public void onCellClick(Cell cell) {
        // TODO
    }

    @Override
    public void onMessage(Message msg) {
        // TODO
    }

    public void onQuitClick(View v) {
        // TODO
    }
}
