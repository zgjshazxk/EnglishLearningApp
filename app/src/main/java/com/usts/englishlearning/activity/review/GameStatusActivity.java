package com.usts.englishlearning.activity.review;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.usts.englishlearning.R;
import com.usts.englishlearning.activity.BaseActivity;
import com.usts.englishlearning.activity.MainActivity;
import com.usts.englishlearning.activity.ShowActivity;
import com.usts.englishlearning.activity.load.LoadGameActivity;
import com.usts.englishlearning.util.ActivityCollector;

public class GameStatusActivity extends BaseActivity {

    public static final String GAME_STATUS = "gameStatus";

    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_FAIL = -1;

    private TextView textStatus;

    private RelativeLayout layoutExit, layoutAgain, layoutReview;

    private ShowActivity showActivity = new ShowActivity();

    private CardView cardGameStatus, cardAgain, cardExit, cardReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_status);

        noNight();

        init();

        cardGameStatus.getBackground().setAlpha(150);
        cardReview.getBackground().setAlpha(180);
        cardExit.getBackground().setAlpha(180);
        cardAgain.getBackground().setAlpha(180);

        int currentStatus = getIntent().getIntExtra(GAME_STATUS, 0);

        if (currentStatus == STATUS_FAIL) {
            textStatus.setText("游戏失败");
        } else {
            textStatus.setText("游戏胜利");
        }

        layoutExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        layoutReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(GameStatusActivity.this, ShowActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(showActivity.SHOW_TYPE, showActivity.TYPE_GAME);
                startActivity(intent);
            }
        });

        layoutAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCollector.startOtherActivity(GameStatusActivity.this, LoadGameActivity.class);
            }
        });

    }

    private void init() {
        textStatus = findViewById(R.id.text_gs_status);
        layoutExit = findViewById(R.id.layout_gs_exit);
        layoutAgain = findViewById(R.id.layout_gs_again);
        layoutReview = findViewById(R.id.layout_gs_review);
        cardGameStatus = findViewById(R.id.card_game_status);
        cardAgain = findViewById(R.id.card_gs_again);
        cardExit = findViewById(R.id.card_gs_exit);
        cardReview = findViewById(R.id.card_gs_review);
    }

    @Override
    public void onBackPressed() {
        ActivityCollector.startOtherActivity(GameStatusActivity.this, MainActivity.class);
    }
}
