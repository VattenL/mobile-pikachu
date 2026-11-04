package ptit.e23cnpm01.pikachu.view;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import ptit.e23cnpm01.pikachu.R;
import ptit.e23cnpm01.pikachu.core.logic.BoardGenerator;
import ptit.e23cnpm01.pikachu.core.logic.Bot;
import ptit.e23cnpm01.pikachu.core.logic.Difficulty;
import ptit.e23cnpm01.pikachu.core.logic.PathFinder;
import ptit.e23cnpm01.pikachu.core.model.Board;
import ptit.e23cnpm01.pikachu.core.model.Cell;
import ptit.e23cnpm01.pikachu.core.protocol.Message;
import ptit.e23cnpm01.pikachu.core.protocol.MessageType;
import ptit.e23cnpm01.pikachu.network.ClientSession;
import ptit.e23cnpm01.pikachu.network.ServerConnection;

/**
 * Giao diện ván chơi với máy (Modul M3 - Play with Bot).
 * Kế thừa BaseActivity để tiếp tục nhận thông điệp từ server trong khi luyện tập.
 *
 * <p>Phụ trách: Dev 1.
 */
public class BotGameActivity extends BaseActivity {

    private static final long MATCH_DURATION_MS = 600_000L; // 10 phút thi đấu
    private static final long TIMER_INTERVAL_MS = 1_000L;   // Cập nhật mỗi giây

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
    private boolean isGameOver = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Đảm bảo cố định màn hình ngang
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        setContentView(R.layout.activity_bot_game);

        initViews();
        setupGame();
    }

    private void initViews() {
        boardView = findViewById(R.id.boardView);
        botBoardView = findViewById(R.id.botBoardView);
        tvTimer = findViewById(R.id.tvTimer);
        tvPlayerRemaining = findViewById(R.id.tvPlayerRemaining);
        tvBotRemaining = findViewById(R.id.tvBotRemaining);
        btnQuit = findViewById(R.id.btnQuit);

        if (btnQuit != null) {
            btnQuit.setOnClickListener(this::onQuitClick);
        }
    }

    private void setupGame() {
        // 1. Đọc độ khó từ Intent
        String difficultyStr = getIntent().getStringExtra("DIFFICULTY");
        Difficulty difficulty = Difficulty.MEDIUM;
        if (difficultyStr != null) {
            try {
                difficulty = Difficulty.valueOf(difficultyStr);
            } catch (IllegalArgumentException ignored) {
                difficulty = Difficulty.MEDIUM;
            }
        }
        bot = new Bot(difficulty);

        // 2. Sinh đề chung xuất phát: vùng chơi 10x10, 25 icon (kích thước lưới 12x12 tính cả viền 0)
        board = BoardGenerator.generateBoard(10, 10, 25, System.currentTimeMillis());
        botBoard = board.copy(); // Bản sao đề bài độc lập cho máy

        // 3. Gắn dữ liệu và quyền tương tác vào View
        boardView.setBoard(board);
        boardView.setInteractive(true);
        boardView.setOnCellClickListener(this::onCellClick);

        botBoardView.setBoard(botBoard);
        botBoardView.setInteractive(false);

        updateRemainingCounts();

        // 4. Báo trạng thái lên Server nếu người chơi đã đăng nhập
        sendStatusUpdate("TRAINING");

        // 5. Khởi động đồng hồ đếm ngược 10 phút
        handler = new Handler(Looper.getMainLooper());
        startTimer();

        // 6. Lập lịch nước đi đầu tiên cho máy
        handler.postDelayed(this::onBotTurn, bot.getDelayMs());
    }

    /**
     * Xử lý tương tác click chọn ô trên bàn cờ của người chơi.
     */
    public void onCellClick(Cell cell) {
        if (isGameOver || cell == null) {
            return;
        }

        // Bỏ qua nếu click vào viền rỗng hoặc ô đã bị ăn
        int tileValue = board.getTile(cell);
        if (tileValue <= 0) {
            return;
        }

        // Trường hợp 1: Chưa chọn ô thứ nhất
        if (firstCell == null) {
            firstCell = cell;
            boardView.setSelectedCell(cell);
            return;
        }

        // Trường hợp 2: Click lại đúng ô đang chọn -> Hủy chọn
        if (firstCell.equals(cell)) {
            firstCell = null;
            boardView.clearSelection();
            return;
        }

        // Trường hợp 3: Chọn ô thứ hai khác loại hình -> Đổi vùng chọn sang ô mới
        if (board.getTile(firstCell) != tileValue) {
            firstCell = cell;
            boardView.setSelectedCell(cell);
            return;
        }

        // Trường hợp 4: Hai ô cùng loại hình -> Kiểm tra đường nối
        ArrayList<Cell> path = PathFinder.checkPath(board, firstCell, cell);
        if (path != null) {
            // Nối thành công: hiển thị line nối trong 300ms
            boardView.showPath(path);
            board.removePair(firstCell, cell);

            // Bỏ đánh dấu chọn
            firstCell = null;
            boardView.clearSelection();

            // Cập nhật số ô còn lại
            updateRemainingCounts();

            // Kiểm tra điều kiện thắng
            if (board.isCleared()) {
                endGame("WIN");
                return;
            }

            // Kiểm tra bế tắc và tự động xáo bàn
            checkAndResolvePlayerDeadlock();
        } else {
            // Không nối được: chuyển chọn sang ô vừa click
            firstCell = cell;
            boardView.setSelectedCell(cell);
        }
    }

    /**
     * Vòng lặp lượt đi của Bot theo chu kỳ trễ getDelayMs().
     */
    public void onBotTurn() {
        if (isGameOver || isFinishing() || isDestroyed()) {
            return;
        }

        // Máy tính toán nước đi
        Cell[] pair = bot.nextMove(botBoard);

        if (pair == null) {
            // Bàn cờ máy bế tắc -> Tự động xáo bàn
            botBoard = BoardGenerator.shuffle(botBoard, System.currentTimeMillis());
            botBoardView.setBoard(botBoard);
            // Lên lịch thử lại ở nhịp tiếp theo
            handler.postDelayed(this::onBotTurn, bot.getDelayMs());
            return;
        }

        // Lấy đường nối trên bàn máy và hiển thị hiệu ứng
        ArrayList<Cell> botPath = PathFinder.checkPath(botBoard, pair[0], pair[1]);
        if (botPath != null) {
            botBoardView.showPath(botPath);
        }

        // Xóa cặp ô trên bàn máy
        botBoard.removePair(pair[0], pair[1]);
        botBoardView.invalidate();
        updateRemainingCounts();

        // Kiểm tra điều kiện máy chiến thắng
        if (botBoard.isCleared()) {
            endGame("LOSE");
            return;
        }

        // Lên lịch nước đi kế tiếp nếu ván đấu chưa kết thúc
        handler.postDelayed(this::onBotTurn, bot.getDelayMs());
    }

    /**
     * Kiểm tra và tự động đảo bàn khi người chơi hết nước đi hợp lệ.
     */
    private void checkAndResolvePlayerDeadlock() {
        if (PathFinder.findAvailablePair(board) == null) {
            Toast.makeText(this, "Không còn nước đi, bàn cờ đang được xáo lại!", Toast.LENGTH_SHORT).show();
            board = BoardGenerator.shuffle(board, System.currentTimeMillis());
            boardView.setBoard(board);
            firstCell = null;
        }
    }

    /**
     * Cập nhật nhãn đếm số ô còn lại trên cả 2 bàn cờ.
     */
    private void updateRemainingCounts() {
        if (tvPlayerRemaining != null && board != null) {
            tvPlayerRemaining.setText(String.valueOf(board.countRemaining()));
        }
        if (tvBotRemaining != null && botBoard != null) {
            tvBotRemaining.setText(String.valueOf(botBoard.countRemaining()));
        }
    }

    /**
     * Bắt đầu đồng hồ đếm ngược 10 phút.
     */
    private void startTimer() {
        timer = new CountDownTimer(MATCH_DURATION_MS, TIMER_INTERVAL_MS) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 1000 / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                if (tvTimer != null) {
                    tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
                }
            }

            @Override
            public void onFinish() {
                if (tvTimer != null) {
                    tvTimer.setText("00:00");
                }
                endGame("DRAW");
            }
        }.start();
    }

    /**
     * Xử lý kết thúc ván: hủy các tiến trình, cập nhật server và mở hộp thoại kết quả.
     *
     * @param result "WIN", "LOSE", hoặc "DRAW"
     */
    private void endGame(String result) {
        if (isGameOver) {
            return;
        }
        isGameOver = true;

        // 1. Hủy Timer đếm ngược
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        // 2. Gỡ bỏ mọi callback lượt đi của Bot trong hàng đợi
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        // 3. Khóa tương tác bàn cờ
        if (boardView != null) {
            boardView.setInteractive(false);
            boardView.clearSelection();
        }

        // 4. Cập nhật trạng thái IDLE về Server
        sendStatusUpdate("IDLE");

        // 5. Hiển thị Dialog kết quả
        showResultDialog(result);
    }

    private void showResultDialog(String result) {
        BotResultDialog dialog = new BotResultDialog(this);
        dialog.setResult(result);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    /**
     * Người chơi chủ động bấm nút "Dừng/Thoát".
     */
    public void onQuitClick(View v) {
        finish();
    }

    @Override
    public void onBackPressed() {
        // Tương đương hành động thoát ván
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        // Đảm bảo dọn dẹp sạch sẽ tài nguyên khi Activity bị hủy
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        if (!isGameOver) {
            sendStatusUpdate("IDLE");
        }
        super.onDestroy();
    }

    /**
     * Gửi bản tin cập nhật trạng thái lên Server qua giao thức TCP.
     */
    private void sendStatusUpdate(String status) {
        if (ClientSession.player != null) {
            try {
                Message msg = new Message(MessageType.UPDATE_STATUS).put("status", status);
                ServerConnection.getInstance().send(msg);
            } catch (Exception ignored) {
                // Đảm bảo ván chơi offline không bị gián đoạn nếu mất kết nối
            }
        }
    }
}