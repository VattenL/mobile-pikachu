package ptit.e23cnpm01.pikachu.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import ptit.e23cnpm01.pikachu.R;
import ptit.e23cnpm01.pikachu.core.logic.Difficulty;

/**
 * Giao diện chọn độ khó.
 *
 * <p>Phụ trách: Dev 1.
 */
public class DifficultyActivity extends BaseActivity {
    private RadioGroup rgDifficulty;
    private Button btnStart;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty);
        rgDifficulty = findViewById(R.id.rgDifficulty);
        btnStart = findViewById(R.id.btnStart);
        btnBack = findViewById(R.id.btnBack);
    }

    public void onStartClick(View v) {
        int checkedId = rgDifficulty != null ? rgDifficulty.getCheckedRadioButtonId() : -1;
        Difficulty difficulty;
        if (checkedId == R.id.rbEasy) {
            difficulty = Difficulty.EASY;
        } else if (checkedId == R.id.rbHard) {
            difficulty = Difficulty.HARD;
        } else {
            difficulty = Difficulty.MEDIUM;
        }
        Intent intent = new Intent(this, BotGameActivity.class);
        intent.putExtra("DIFFICULTY", difficulty.name());
        startActivity(intent);
        finish();
    }

    public void onBackClick(View v) {
        finish();
    }
}
