package ptit.e23cnpm01.pikachu.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import ptit.e23cnpm01.pikachu.core.model.Board;
import ptit.e23cnpm01.pikachu.core.model.Cell;

/**
 * Vẽ bàn chơi 10x10, chọn ô, vẽ đường nối.
 *
 * <p>Phụ trách: Dev 1.
 */
public class BoardView extends View {
    private Board board;
    private ArrayList<Cell> path;

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setBoard(Board board) {
        // TODO
    }

    public void setInteractive(boolean b) {
        // TODO
    }

    public void showPath(ArrayList<Cell> path) {
        // TODO
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // TODO
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // TODO
        return super.onTouchEvent(e);
    }
}
