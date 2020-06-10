package com.usts.englishlearning.activity.review;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.usts.englishlearning.R;
import com.usts.englishlearning.activity.BaseActivity;
import com.usts.englishlearning.activity.MainActivity;
import com.usts.englishlearning.adapter.MeanChoiceAdapter;
import com.usts.englishlearning.database.Word;
import com.usts.englishlearning.entity.GameWord;
import com.usts.englishlearning.entity.ItemWordMeanChoice;
import com.usts.englishlearning.listener.OnItemClickListener;
import com.usts.englishlearning.util.ActivityCollector;
import com.usts.englishlearning.util.MediaHelper;
import com.usts.englishlearning.util.NumberController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GameActivity extends BaseActivity {

    public static ArrayList<Integer> alreadyWords = new ArrayList<>();

    // 底部的操作栏
    private RecyclerView recyclerView;

    private List<ItemWordMeanChoice> meanChoiceList = new ArrayList<>();

    private MeanChoiceAdapter meanChoiceAdapter;

    private ProgressBar progressBar;

    private Handler handler1, handler2;

    private Runnable runnable1, runnable2;

    // 猫的进度
    private int progressCat = 0;

    // 老鼠的进度
    private int progressMouse = 800;

    // 猫平时增加的进度
    private int addCat = 2;

    // 老鼠平时增加的进度
    private int addMouse = 1;

    // 回答正确时老鼠增加的进度
    private int addRightMouse = 200;

    // 回答错误时猫增加的进度
    private int addWrongCat = 70;

    // 没有及时回答时猫增加的进度
    private int addNACat = 30;

    // 规定剩余时间
    private int remainTime = 5;

    private TextView textTime, textWord;

    private boolean inFinish = false;

    private GameWord currentWord;

    // 当前展示的单词下标
    private int currentIndex = 0;

    // 所有单词
    // 提供游戏单词
    public static List<Word> allWord = new ArrayList<>();

    // 游戏里面的单词
    // 从上面的单词中抽取50个
    public static List<GameWord> gameWord = new ArrayList<>();

    private static final String TAG = "GameActivity";

    private int progressWidth;

    private CircleImageView imgCat, imgMouse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        init();

        MediaHelper.playLocalFileRepeat(R.raw.game);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        meanChoiceAdapter = new MeanChoiceAdapter(meanChoiceList);
        recyclerView.setAdapter(meanChoiceAdapter);

        setWordMeanData();

        progressBar.setProgress(progressCat);
        progressBar.setSecondaryProgress(progressMouse);
        progressBar.setMax(10000);

        progressBar.post(new Runnable() {
            @Override
            public void run() {
                progressWidth = progressBar.getWidth();
            }
        });

        handler1 = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                runnable1 = new Runnable() {

                    @Override
                    public void run() {
                        if (!inFinish) {
                            handler1.postDelayed(this, 10);
                            // 设置猫和老鼠的进度
                            progressCat = progressCat + addCat;
                            progressMouse = progressMouse + addMouse;
                            progressBar.setProgress(progressCat);
                            progressBar.setSecondaryProgress(progressMouse);
                            // 设置头像便宜
                            imgCat.setTranslationX((float) progressCat / progressBar.getMax() * progressWidth);
                            imgMouse.setTranslationX((float) progressMouse / progressBar.getMax() * progressWidth);
                            // 说明猫已经追赶上老鼠了
                            if (progressMouse <= progressCat) {
                                stopTime1();
                                inFinish = true;
                                Intent intent = new Intent(GameActivity.this, GameStatusActivity.class);
                                intent.putExtra(GameStatusActivity.GAME_STATUS, GameStatusActivity.STATUS_FAIL);
                                startActivity(intent);
                                finish();
                                // 老鼠已达到顶点
                            } else if (progressMouse >= progressBar.getMax()) {
                                stopTime1();
                                inFinish = true;
                                Intent intent = new Intent(GameActivity.this, GameStatusActivity.class);
                                intent.putExtra(GameStatusActivity.GAME_STATUS, GameStatusActivity.STATUS_SUCCESS);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }

                };
                handler1.postDelayed(runnable1, 10);
            }
        }).start();

        meanChoiceAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView parent, View view, int position, ItemWordMeanChoice itemWordMeanChoice) {
                if (MeanChoiceAdapter.isFirstClick) {
                    // 答错了
                    if (itemWordMeanChoice.getId() != currentWord.getId()) {
                        answerWrong();
                        itemWordMeanChoice.setRight(ItemWordMeanChoice.WRONG);
                        meanChoiceAdapter.notifyDataSetChanged();
                        MeanChoiceAdapter.isFirstClick = false;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                MeanChoiceAdapter.isFirstClick = true;
                                setWordMeanData();
                            }
                        }, 250);
                    } else {
                        // 答对了
                        answerRight();
                        itemWordMeanChoice.setRight(ItemWordMeanChoice.RIGHT);
                        meanChoiceAdapter.notifyDataSetChanged();
                        MeanChoiceAdapter.isFirstClick = false;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                MeanChoiceAdapter.isFirstClick = true;
                                setWordMeanData();
                            }
                        }, 250);
                    }
                }
            }
        });

        handler2 = new Handler();
        runnable2 = new Runnable() {

            @Override
            public void run() {
                textTime.setText("剩余时间：" + remainTime--);
                handler2.postDelayed(this, 1000);
                if (inFinish)
                    stopTime2();
                if (remainTime == -1) {
                    noAnswer();
                    remainTime = 5;
                    setWordMeanData();
                }
            }

        };
        handler2.postDelayed(runnable2, 10);

    }

    private void init() {
        recyclerView = findViewById(R.id.recycler_gm_bottom);
        progressBar = findViewById(R.id.progress_gm);
        textTime = findViewById(R.id.text_gm_time);
        textWord = findViewById(R.id.text_gm_word);
        imgCat = findViewById(R.id.img_gm_cat);
        imgMouse = findViewById(R.id.img_gm_mouse);
    }

    private void setWordMeanData() {
        setRandomWord();
        if (!meanChoiceList.isEmpty())
            meanChoiceList.clear();
        alreadyWords.add(currentWord.getId());
        meanChoiceList.add(new ItemWordMeanChoice(currentWord.getId(), currentWord.getWordMeans(), ItemWordMeanChoice.NOTSTART));
        int[] randomIds = NumberController.getRandomExceptList(0, gameWord.size() - 1, 3, currentIndex);
        Log.d(TAG, "currentIndex:" + currentIndex);
        Log.d(TAG, "size:" + gameWord.size());
        for (int i = 0; i < randomIds.length; ++i) {
            Log.d(TAG, "otherIndex:" + randomIds[i]);
            meanChoiceList.add(new ItemWordMeanChoice(gameWord.get(randomIds[i]).getId(),
                    gameWord.get(randomIds[i]).getWordMeans(),
                    ItemWordMeanChoice.NOTSTART));
        }
        Collections.shuffle(meanChoiceList);
        meanChoiceAdapter.notifyDataSetChanged();
    }

    private void setRandomWord() {
        currentWord = gameWord.get(currentIndex);
        textWord.setText(currentWord.getWordName());
    }

    // 停止计时器
    private void stopTime1() {
        handler1.removeCallbacks(runnable1);
    }

    private void stopTime2() {
        handler2.removeCallbacks(runnable2);
    }

    private void answerRight() {
        if (progressMouse + addRightMouse == progressBar.getMax())
            progressMouse = progressBar.getMax();
        else
            progressMouse = progressMouse + addRightMouse;
        remainTime = 5;
        currentIndex++;
    }

    private void answerWrong() {
        progressCat = progressCat + addWrongCat;
        remainTime = 5;
        currentIndex++;
    }

    private void noAnswer() {
        progressCat = progressCat + addNACat;
        currentIndex++;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MediaHelper.releasePlayer();
        ActivityCollector.startOtherActivity(GameActivity.this, MainActivity.class);
        inFinish = true;
        stopTime1();
        stopTime2();
        alreadyWords.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaHelper.releasePlayer();
        gameWord.clear();
    }
}
