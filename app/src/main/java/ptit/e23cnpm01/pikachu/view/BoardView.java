package ptit.e23cnpm01.pikachu.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ptit.e23cnpm01.pikachu.core.model.Board;
import ptit.e23cnpm01.pikachu.core.model.Cell;

/**
 * View bàn chơi tự vẽ: ma trận lưới 12x12, hiển thị ô hình Pokemon,
 * đánh dấu ô đang chọn, hiệu ứng vẽ đường nối ăn cặp biến mất sau 300ms.
 *
 * <p>Phụ trách: Dev 1.
 */
public class BoardView extends View {

    /** Interface lắng nghe sự kiện click chọn ô trên bàn cờ. */
    public interface OnCellClickListener {
        void onCellClick(Cell cell);
    }

    // Bảng 25 màu sắc phân biệt cho 25 mã icon nếu chưa có file ảnh drawable
    private static final int[] FALLBACK_COLORS = {
            0xFFE57373, 0xFFF06292, 0xFFBA68C8, 0xFF9575CD, 0xFF7986CB,
            0xFF64B5F6, 0xFF4FC3F7, 0xFF4DD0E1, 0xFF4DB6AC, 0xFF81C784,
            0xFFAED581, 0xFFFF8A65, 0xFFFFD54F, 0xFFFFB74D, 0xFFA1887F,
            0xFF90A4AE, 0xFF00ACC1, 0xFF00897B, 0xFF43A047, 0xFF7CB342,
            0xFFC0CA33, 0xFFFDD835, 0xFFFFB300, 0xFFFB8C00, 0xFFF4511E
    };

    private Board board;
    private Cell selectedCell;
    private List<Cell> path;
    private boolean interactive = true;
    private OnCellClickListener cellClickListener;

    // Kích thước vẽ
    private float cellSize;
    private float offsetX;
    private float offsetY;

    // Các đối tượng Paint dùng lại trong onDraw (tránh cấp phát bộ nhớ liên tục)
    private Paint tileBgPaint;
    private Paint tileBorderPaint;
    private Paint selectedBorderPaint;
    private Paint pathPaint;
    private Paint pathNodePaint;
    private Paint textPaint;
    private final RectF tileRect = new RectF();
    private final Path drawPath = new Path();

    // Cache hình ảnh Bitmap đã tải
    private final SparseArray<Bitmap> bitmapCache = new SparseArray<>();

    // Runnable xoá đường nối sau 300ms
    private final Runnable clearPathRunnable = new Runnable() {
        @Override
        public void run() {
            path = null;
            invalidate();
        }
    };

    public BoardView(Context context) {
        super(context);
        init();
    }

    public BoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        tileBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tileBgPaint.setStyle(Paint.Style.FILL);

        tileBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tileBorderPaint.setStyle(Paint.Style.STROKE);
        tileBorderPaint.setColor(0x33000000);
        tileBorderPaint.setStrokeWidth(2f);

        selectedBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedBorderPaint.setStyle(Paint.Style.STROKE);
        selectedBorderPaint.setColor(0xFFFF7043); // Màu cam nổi bật khi chọn

        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setColor(0xFFFF1744); // Màu đỏ neon cho đường nối
        pathPaint.setStrokeCap(Paint.Cap.ROUND);
        pathPaint.setStrokeJoin(Paint.Join.ROUND);

        pathNodePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathNodePaint.setStyle(Paint.Style.FILL);
        pathNodePaint.setColor(0xFFFF1744);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    /** Gán dữ liệu bàn cờ mới và vẽ lại. */
    public void setBoard(Board board) {
        this.board = board;
        this.selectedCell = null;
        this.path = null;
        removeCallbacks(clearPathRunnable);
        calculateDimensions(getWidth(), getHeight());
        invalidate();
    }

    public Board getBoard() {
        return board;
    }

    /** Đặt quyền tương tác: true cho bàn người chơi, false cho bàn bot thu nhỏ. */
    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }

    public boolean isInteractive() {
        return interactive;
    }

    /** Đánh dấu ô đang được chọn hoặc truyền null để bỏ chọn. */
    public void setSelectedCell(Cell cell) {
        this.selectedCell = cell;
        invalidate();
    }

    public Cell getSelectedCell() {
        return selectedCell;
    }

    public void clearSelection() {
        this.selectedCell = null;
        invalidate();
    }

    /** Đăng ký listener sự kiện click ô. */
    public void setOnCellClickListener(OnCellClickListener listener) {
        this.cellClickListener = listener;
    }

    /** Hiển thị hiệu ứng đường nối ăn cặp trong 300ms rồi tự biến mất. */
    public void showPath(List<Cell> path) {
        if (path == null || path.isEmpty()) return;
        this.path = new ArrayList<>(path);
        removeCallbacks(clearPathRunnable);
        invalidate();
        postDelayed(clearPathRunnable, 300);
    }

    /** Tương thích với kiểu ArrayList<Cell> gốc. */
    public void showPath(ArrayList<Cell> path) {
        showPath((List<Cell>) path);
    }

    /** Dành cho bàn Bot hiển thị nước đi ăn cặp. */
    public void showMatch(List<Cell> path) {
        showPath(path);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateDimensions(w, h);
    }

    private void calculateDimensions(int w, int h) {
        if (w <= 0 || h <= 0) return;
        int cols = (board != null) ? board.getColumnCount() : 12;
        int rows = (board != null) ? board.getRowCount() : 12;

        cellSize = Math.min((float) w / cols, (float) h / rows);
        offsetX = (w - cellSize * cols) / 2f;
        offsetY = (h - cellSize * rows) / 2f;

        selectedBorderPaint.setStrokeWidth(Math.max(3f, cellSize * 0.08f));
        pathPaint.setStrokeWidth(Math.max(4f, cellSize * 0.12f));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (board == null) return;

        if (cellSize <= 0) {
            calculateDimensions(getWidth(), getHeight());
            if (cellSize <= 0) return;
        }

        int rows = board.getRowCount();
        int cols = board.getColumnCount();
        int[][] tiles = board.getTiles();

        float margin = cellSize * 0.06f;
        float cornerRadius = cellSize * 0.16f;

        // 1. Vẽ các ô cờ trên bàn
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int tileVal = tiles[r][c];
                if (tileVal <= 0) continue; // Ô trống hoặc viền 0 không vẽ

                float left = offsetX + c * cellSize + margin;
                float top = offsetY + r * cellSize + margin;
                float right = offsetX + (c + 1) * cellSize - margin;
                float bottom = offsetY + (r + 1) * cellSize - margin;
                tileRect.set(left, top, right, bottom);

                Bitmap bitmap = getIconBitmap(tileVal);
                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, null, tileRect, null);
                } else {
                    // Fallback: vẽ card màu bo góc và số mã icon
                    int colorIdx = (tileVal - 1) % FALLBACK_COLORS.length;
                    tileBgPaint.setColor(FALLBACK_COLORS[colorIdx]);
                    canvas.drawRoundRect(tileRect, cornerRadius, cornerRadius, tileBgPaint);
                    canvas.drawRoundRect(tileRect, cornerRadius, cornerRadius, tileBorderPaint);

                    textPaint.setTextSize(cellSize * 0.38f);
                    float textY = tileRect.centerY() - ((textPaint.descent() + textPaint.ascent()) / 2);
                    canvas.drawText(String.valueOf(tileVal), tileRect.centerX(), textY, textPaint);
                }

                // Vẽ viền highlight nếu ô đang được người chơi chọn
                if (selectedCell != null && selectedCell.row == r && selectedCell.col == c) {
                    canvas.drawRoundRect(tileRect, cornerRadius, cornerRadius, selectedBorderPaint);
                }
            }
        }

        // 2. Vẽ đường nối ăn cặp nếu đang có hiệu ứng showPath
        if (path != null && path.size() >= 2) {
            drawPath.reset();
            for (int i = 0; i < path.size(); i++) {
                Cell pt = path.get(i);
                float cx = offsetX + (pt.col + 0.5f) * cellSize;
                float cy = offsetY + (pt.row + 0.5f) * cellSize;
                if (i == 0) {
                    drawPath.moveTo(cx, cy);
                } else {
                    drawPath.lineTo(cx, cy);
                }
            }
            canvas.drawPath(drawPath, pathPaint);

            // Vẽ điểm mút ở hai đầu đường nối
            Cell start = path.get(0);
            Cell end = path.get(path.size() - 1);
            float nodeRadius = cellSize * 0.16f;
            canvas.drawCircle(offsetX + (start.col + 0.5f) * cellSize, offsetY + (start.row + 0.5f) * cellSize, nodeRadius, pathNodePaint);
            canvas.drawCircle(offsetX + (end.col + 0.5f) * cellSize, offsetY + (end.row + 0.5f) * cellSize, nodeRadius, pathNodePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!interactive || board == null) {
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (cellSize <= 0) return false;

            float x = event.getX();
            float y = event.getY();

            int col = (int) ((x - offsetX) / cellSize);
            int row = (int) ((y - offsetY) / cellSize);

            if (row >= 0 && row < board.getRowCount() && col >= 0 && col < board.getColumnCount()) {
                // Chỉ tiếp nhận chạm vào ô còn icon (> 0)
                if (board.getTile(row, col) > 0) {
                    Cell clicked = new Cell(row, col);
                    dispatchCellClick(clicked);
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    /** Điều hướng sự kiện click tới listener hoặc Activity gọi nó. */
    private void dispatchCellClick(Cell cell) {
        if (cellClickListener != null) {
            cellClickListener.onCellClick(cell);
        } else if (getContext() instanceof BotGameActivity) {
            ((BotGameActivity) getContext()).onCellClick(cell);
        } else if (getContext() instanceof MatchActivity) {
            ((MatchActivity) getContext()).onCellClick(cell);
        }
    }

    /** Nạp và cache ảnh icon từ drawable theo quy ước tên: icon_X hoặc piece_X. */
    @Nullable
    private Bitmap getIconBitmap(int tileVal) {
        if (bitmapCache.indexOfKey(tileVal) >= 0) {
            return bitmapCache.get(tileVal);
        }

        int resId = getResources().getIdentifier("icon_" + tileVal, "drawable", getContext().getPackageName());
        if (resId == 0) {
            resId = getResources().getIdentifier("piece_" + tileVal, "drawable", getContext().getPackageName());
        }

        Bitmap bitmap = null;
        if (resId != 0) {
            try {
                bitmap = BitmapFactory.decodeResource(getResources(), resId);
            } catch (Exception ignored) {
            }
        }
        bitmapCache.put(tileVal, bitmap);
        return bitmap;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(clearPathRunnable);
        bitmapCache.clear();
    }
}