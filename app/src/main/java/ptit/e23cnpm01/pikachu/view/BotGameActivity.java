package ptit.e23cnpm01.pikachu.view;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import ptit.e23cnpm01.pikachu.core.logic.Bot;
import ptit.e23cnpm01.pikachu.core.model.Board;
import ptit.e23cnpm01.pikachu.core.model.Cell;

/**
 * Giao diện chơi với máy.
 *
 * <p>Phụ trách: Dev 1.
 */
public class BotGameActivity extends BaseActivity {
    private BoardView boardView;
    private BoardView botBoardView;
    private TextView tvTimer;
    private TextView tvPlayerRemaining;
    private TextView tvBotRemaining;
    private Button btnQuit;
    private Board board;
    private Board botBoard;
    private Bot bot;
    private Cell firstCell;
    private CountDownTimer timer;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: setContentView, gắn view
    }

    public void onCellClick(Cell cell) {
        // TODO
    }

    public void onBotTurn() {
        // TODO
    }

    public void onQuitClick(View v) {
        // TODO
    }

    private void endGame(String result) {
        // TODO
    }
}
