package com.usts.englishlearning.activity.load;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.usts.englishlearning.R;
import com.usts.englishlearning.activity.BaseActivity;
import com.usts.englishlearning.activity.MainActivity;
import com.usts.englishlearning.activity.review.GameActivity;
import com.usts.englishlearning.activity.review.SpeedActivity;
import com.usts.englishlearning.config.ConfigData;
import com.usts.englishlearning.database.Interpretation;
import com.usts.englishlearning.database.Word;
import com.usts.englishlearning.entity.GameWord;
import com.usts.englishlearning.util.ActivityCollector;
import com.usts.englishlearning.util.CustomVideoView;
import com.usts.englishlearning.util.MediaHelper;
import com.usts.englishlearning.util.NumberController;
import com.usts.englishlearning.util.TimeController;

import org.litepal.LitePal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LoadGameActivity extends BaseActivity {

    private ProgressBar progressBar;

    private ImageView imgPlay;

    private CustomVideoView videoView;

    int num = 50;

    private final int FINISH = 1;

    private static final String TAG = "LoadGameActivity";

    private boolean isDone = false;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case FINISH:
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            imgPlay.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            isDone = true;
                        }
                    }, 2000);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_game);

        Log.d(TAG, "onCreate: ");

        noNight();


        init();

        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video));
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.start();
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.start();
            }
        });


        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Word> words = LitePal.select("wordId", "word").find(Word.class);
                Collections.shuffle(words);
                GameActivity.allWord = words;
                int[] randomIndex = NumberController.getRandomNumberList(0, GameActivity.allWord.size() - 1, num);
                Log.d(TAG, "content" + Arrays.toString(randomIndex));
                for (int i = 0; i < randomIndex.length; ++i) {
                    Interpretation interpretation = LitePal.where("wordId = ?", GameActivity.allWord.get(randomIndex[i]).getWordId() + "").find(Interpretation.class).get(0);
                    GameActivity.gameWord.add(new GameWord(
                            GameActivity.allWord.get(randomIndex[i]).getWordId(),
                            GameActivity.allWord.get(randomIndex[i]).getWord(),
                            interpretation.getWordType() + ". " + interpretation.getCHSMeaning()));
                }
                // 打乱顺序
                Collections.shuffle(GameActivity.allWord);
                Message message = new Message();
                message.what = FINISH;
                handler.sendMessage(message);
            }
        }).start();

        imgPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaHelper.releasePlayer();
                ActivityCollector.startOtherActivity(LoadGameActivity.this, GameActivity.class);
                finish();
            }
        });

    }

    private void init() {
        progressBar = findViewById(R.id.progress_ldg);
        progressBar.setVisibility(View.VISIBLE);
        videoView = findViewById(R.id.video);
        imgPlay = findViewById(R.id.img_play);
        imgPlay.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        MainActivity.lastFragment = 1;
        MainActivity.needRefresh = false;
        super.onBackPressed();
        MediaHelper.releasePlayer();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView != null)
            videoView.suspend();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoView.suspend();
    }

    @Override
    protected void onStart() {
        super.onStart();
        videoView.resume();
        if (isDone) {
            imgPlay.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        } else {
            imgPlay.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

}
