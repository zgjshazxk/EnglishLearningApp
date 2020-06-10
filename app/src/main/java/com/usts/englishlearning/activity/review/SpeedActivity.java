package com.usts.englishlearning.activity.review;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.usts.englishlearning.R;
import com.usts.englishlearning.activity.BaseActivity;
import com.usts.englishlearning.activity.ListActivity;
import com.usts.englishlearning.activity.ShowActivity;
import com.usts.englishlearning.config.ConfigData;
import com.usts.englishlearning.config.ConstantData;
import com.usts.englishlearning.database.Interpretation;
import com.usts.englishlearning.database.Word;
import com.usts.englishlearning.util.MyApplication;
import com.usts.englishlearning.util.NumberController;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import me.grantland.widget.AutofitTextView;

public class SpeedActivity extends BaseActivity implements View.OnClickListener {

    private CardView cardImg;

    private ObjectAnimator objectAnimator;

    private RelativeLayout layoutPause, layoutHome;

    private LinearLayout layoutWord;

    private TextView textProgress, textPhone, textMean, textPause;

    private AutofitTextView textWord;

    // 单词列表
    public static ArrayList<Word> wordList = new ArrayList<>();

    private MediaPlayer mediaPlayer;

    private int tem = 0;

    private boolean isPause = false;

    private ShowActivity showActivity = new ShowActivity();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed);

        init();

        windowExplode();

        initAnimation();

        showWord();
        playWord();

    }

    private void init() {
        cardImg = findViewById(R.id.card_sp_circle);
        cardImg.setOnClickListener(this);
        layoutPause = findViewById(R.id.layout_sp_pause);
        layoutPause.setOnClickListener(this);
        layoutHome = findViewById(R.id.layout_sp_home);
        layoutHome.setOnClickListener(this);
        layoutWord = findViewById(R.id.layout_sp_word);
        layoutWord.setOnClickListener(this);
        textProgress = findViewById(R.id.text_sp_top);
        textPhone = findViewById(R.id.text_ls_phone);
        textMean = findViewById(R.id.text_ls_mean);
        textWord = findViewById(R.id.text_ls_word);
        textPause = findViewById(R.id.text_sp_pause);
    }

    private void initAnimation() {
        objectAnimator = ObjectAnimator.ofFloat(cardImg, "rotation", 0.0f, 360.0f);
        objectAnimator.setDuration(25000);
        // 无限循环
        objectAnimator.setRepeatCount(Animation.INFINITE);
        objectAnimator.setRepeatMode(ObjectAnimator.RESTART);
        // 匀速
        objectAnimator.setInterpolator(new LinearInterpolator());
        // 开始动画
        objectAnimator.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_sp_pause:
                if (!isPause) {
                    isPause = true;
                    objectAnimator.pause();
                    textPause.setText("继续");
                } else {
                    isPause = false;
                    objectAnimator.resume();
                    textPause.setText("暂停");
                    tem++;
                    playWord();
                }
                break;
            case R.id.layout_sp_home:
                onBackPressed();
                break;
            case R.id.layout_sp_word:
                cardImg.setVisibility(View.VISIBLE);
                layoutWord.setVisibility(View.GONE);
                break;
            case R.id.card_sp_circle:
                cardImg.setVisibility(View.GONE);
                layoutWord.setVisibility(View.VISIBLE);
                break;
        }
    }

    // 播放单词
    private void playWord() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            mediaPlayer = new MediaPlayer();
        } else
            mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(ConstantData.YOU_DAO_VOICE_EN + wordList.get(tem).getWord());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (!isPause) {
                        mediaPlayer.start();
                        showWord();
                    }
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (tem != wordList.size() - 1) {
                        if (!isPause) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    ++tem;
                                    playWord();
                                }
                            }, 1000);
                        }
                    } else {
                        Toast.makeText(SpeedActivity.this, "播放完毕", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.setClass(SpeedActivity.this, ShowActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(showActivity.SHOW_TYPE, showActivity.TYPE_SPEED);
                        startActivity(intent);
                    }
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Toast.makeText(MyApplication.getContext(), "发生错误，即将返回，请检查联网设置", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showWord() {
        textProgress.setText((tem + 1) + " / " + wordList.size());
        textWord.setText(wordList.get(tem).getWord());
        List<Interpretation> interpretations = LitePal.where("wordId = ?", wordList.get(tem).getWordId() + "").find(Interpretation.class);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < interpretations.size(); ++i) {
            if (i != (interpretations.size() - 1))
                stringBuilder.append(interpretations.get(i).getWordType() + ". " + interpretations.get(i).getCHSMeaning() + "\n");
            else
                stringBuilder.append(interpretations.get(i).getWordType() + ". " + interpretations.get(i).getCHSMeaning());
        }
        textPhone.setText(wordList.get(tem).getUkPhone());
        textMean.setText(stringBuilder.toString());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isPause = true;
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

}
