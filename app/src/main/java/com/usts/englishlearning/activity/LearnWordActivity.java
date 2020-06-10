package com.usts.englishlearning.activity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.usts.englishlearning.R;
import com.usts.englishlearning.adapter.MeanChoiceAdapter;
import com.usts.englishlearning.database.Interpretation;
import com.usts.englishlearning.database.LearnTime;
import com.usts.englishlearning.database.Sentence;
import com.usts.englishlearning.database.Word;
import com.usts.englishlearning.entity.ItemWordMeanChoice;
import com.usts.englishlearning.listener.OnItemClickListener;
import com.usts.englishlearning.util.ActivityCollector;
import com.usts.englishlearning.util.MediaHelper;
import com.usts.englishlearning.util.TimeController;
import com.usts.englishlearning.util.WordController;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LearnWordActivity extends BaseActivity implements View.OnClickListener {

    private RecyclerView recyclerView;

    private RelativeLayout layoutDelete, layoutVoice, layoutTip;

    private List<ItemWordMeanChoice> wordMeanChoices = new ArrayList<>();

    private LinearLayout layoutBottomReview;

    private LinearLayout layoutBottomLearn;

    private TextView textWord, textWordPhone;

    private TextView textLastWord, textLastWordMean;

    private int[] randomId;

    private RelativeLayout cardKnow, cardNotKnow, cardDark;

    private CardView cardTip;

    private TextView textTip;

    // 记录上一个单词
    public static String lastWord;
    public static String lastWordMean;

    private TextView textLearnNum, textReviewNum;

    private MeanChoiceAdapter meanChoiceAdapter;

    private static final String TAG = "LearnWordActivity";

    public static boolean needUpdate = true;

    // 学习时间记录
    private long startTime = -1;

    public static final String MODE_NAME = "learnmode";

    public static final int MODE_GENERAL = 1;
    public static final int MODE_ONCE = 2;

    private int currentMode;

    private String tipSentence;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_word);

        init();

        currentMode = getIntent().getIntExtra(MODE_NAME, MODE_GENERAL);

        startTime = TimeController.getCurrentTimeStamp();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        meanChoiceAdapter = new MeanChoiceAdapter(wordMeanChoices);

        meanChoiceAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView parent, View view, int position, ItemWordMeanChoice itemWordMeanChoice) {
                if (MeanChoiceAdapter.isFirstClick) {
                    Log.d(TAG, "选择了：" + itemWordMeanChoice.getId());
                    Log.d(TAG, "目标是： " + WordController.currentWordId);
                    // 答错了
                    if (itemWordMeanChoice.getId() != WordController.currentWordId) {
                        switch (WordController.currentMode) {
                            case WordController.REVIEW_AT_TIME:
                                WordController.reviewNewWordDone(WordController.currentWordId, false);
                                break;
                            case WordController.REVIEW_GENERAL:
                                WordController.reviewOneWordDone(WordController.currentWordId, false);
                                break;
                        }
                        itemWordMeanChoice.setRight(ItemWordMeanChoice.WRONG);
                        meanChoiceAdapter.notifyDataSetChanged();
                        MeanChoiceAdapter.isFirstClick = false;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                WordDetailActivity.wordId = WordController.currentWordId;
                                Intent intent = new Intent();
                                intent.setClass(LearnWordActivity.this, WordDetailActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra(WordDetailActivity.TYPE_NAME, WordDetailActivity.TYPE_LEARN);
                                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(LearnWordActivity.this).toBundle());
                                MeanChoiceAdapter.isFirstClick = true;
                            }
                        }, 250);
                    } else {
                        switch (WordController.currentMode) {
                            case WordController.REVIEW_AT_TIME:
                                WordController.reviewNewWordDone(WordController.currentWordId, true);
                                break;
                            case WordController.REVIEW_GENERAL:
                                WordController.reviewOneWordDone(WordController.currentWordId, true);
                                break;
                        }
                        itemWordMeanChoice.setRight(ItemWordMeanChoice.RIGHT);
                        meanChoiceAdapter.notifyDataSetChanged();
                        MeanChoiceAdapter.isFirstClick = false;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                updateStatus();
                                MeanChoiceAdapter.isFirstClick = true;
                            }
                        }, 250);
                    }
                    Log.d(TAG, "id+" + itemWordMeanChoice.getId());
                }
            }
        });

        recyclerView.setAdapter(meanChoiceAdapter);

    }

    private void init() {
        recyclerView = findViewById(R.id.recyclerview_word_mean);
        layoutBottomReview = findViewById(R.id.layout_word_bottom);
        layoutBottomLearn = findViewById(R.id.linear_learn_control);
        textWord = findViewById(R.id.text_learn_word);
        textWordPhone = findViewById(R.id.text_learn_word_phone);
        cardDark = findViewById(R.id.card_dark);
        cardDark.setOnClickListener(this);
        cardKnow = findViewById(R.id.card_know);
        cardKnow.setOnClickListener(this);
        cardNotKnow = findViewById(R.id.card_no_know);
        cardNotKnow.setOnClickListener(this);
        textLearnNum = findViewById(R.id.text_new_num_top);
        textReviewNum = findViewById(R.id.text_review_num_top);
        textLastWord = findViewById(R.id.text_word_top);
        textLastWordMean = findViewById(R.id.text_word_top_mean);
        layoutTip = findViewById(R.id.layout_word_tip);
        layoutTip.setOnClickListener(this);
        layoutDelete = findViewById(R.id.layout_word_delete);
        layoutDelete.setOnClickListener(this);
        layoutVoice = findViewById(R.id.layout_word_voice);
        layoutVoice.setOnClickListener(this);
        cardTip = findViewById(R.id.card_lw_tip);
        textTip = findViewById(R.id.text_lw_tip);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.card_dark:
                if (!TextUtils.isEmpty(tipSentence.trim())) {
                    textTip.setText(tipSentence);
                    cardTip.setVisibility(View.VISIBLE);
                    MediaHelper.play(tipSentence);
                } else {
                    Toast.makeText(this, "暂无提示", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.card_no_know:
                WordDetailActivity.wordId = WordController.currentWordId;
                Intent intent = new Intent();
                intent.setClass(LearnWordActivity.this, WordDetailActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(WordDetailActivity.TYPE_NAME, WordDetailActivity.TYPE_LEARN);
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(LearnWordActivity.this).toBundle());
                WordController.learnNewWordDone(WordController.currentWordId);
                break;
            case R.id.card_know:
                WordController.learnNewWordDone(WordController.currentWordId);
                updateStatus();
                break;
            case R.id.layout_word_tip:
                ActivityCollector.startOtherActivity(LearnWordActivity.this, WordDetailActivity.class);
                WordDetailActivity.wordId = WordController.currentWordId;
                break;
            case R.id.layout_word_delete:
                WordController.removeOneWord(WordController.currentWordId);
                updateStatus();
                break;
            case R.id.layout_word_voice:
                MediaHelper.play(textWord.getText().toString());
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(LearnWordActivity.this, MainActivity.class);
        startActivity(intent);
        MediaHelper.releasePlayer();
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (needUpdate) {
            updateStatus();
            needUpdate = false;
        }
    }

    public void updateStatus() {
        tipSentence = "";
        cardTip.setVisibility(View.GONE);
        textLearnNum.setText("新学" + WordController.needLearnWords.size());
        textReviewNum.setText("复习" + (WordController.needReviewWords.size() + WordController.justLearnedWords.size()));
        WordController.currentMode = WordController.whatToDo();
        switch (WordController.currentMode) {
            case WordController.REVIEW_AT_TIME:
                WordController.currentWordId = WordController.reviewNewWord();
                showReview();
                break;
            case WordController.REVIEW_GENERAL:
                WordController.currentWordId = WordController.reviewOneWord();
                showReview();
                break;
            case WordController.NEW_LEARN:
                WordController.currentWordId = WordController.learnNewWord();
                showLearn();
                break;
            case WordController.TODAY_MASK_DONE:
                switch (currentMode) {
                    case MODE_GENERAL:
                        Toast.makeText(this, "已完成今日任务", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, FinishActivity.class);
                        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(LearnWordActivity.this).toBundle());
                        finish();
                        break;
                    case MODE_ONCE:
                        Toast.makeText(this, "复习完毕", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                        break;
                }
                break;
        }

        Log.d(TAG, "currentId" + WordController.currentWordId);

        // 找到该单词的数据
        List<Word> words = LitePal.where("wordId = ?", WordController.currentWordId + "").select("wordId", "word", "ukPhone", "usPhone").find(Word.class);
        if (!words.isEmpty()) {
            Word word = words.get(0);
            textWord.setText(word.getWord());
            if (word.getUsPhone() != null)
                textWordPhone.setText(word.getUsPhone());
            else
                textWordPhone.setText(word.getUkPhone());

            if (WordController.currentMode != WordController.TODAY_MASK_DONE)
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MediaHelper.play(textWord.getText().toString());
                    }
                }).start();

            // 得到该单词的释义
            List<Interpretation> interpretations = LitePal.where("wordId = ?", WordController.currentWordId + "").find(Interpretation.class);
            StringBuilder stringBuilder = new StringBuilder();
            if (!interpretations.isEmpty()) {
                stringBuilder.append(interpretations.get(0).getWordType() + ". " + interpretations.get(0).getCHSMeaning());
            }

            // 得到该单词的例句
            List<Sentence> sentences = LitePal.where("wordId = ?", WordController.currentWordId + "").find(Sentence.class);
            if (!sentences.isEmpty())
                tipSentence = sentences.get(0).getEnSentence();

            if (WordController.currentMode == WordController.REVIEW_AT_TIME ||
                    WordController.currentMode == WordController.REVIEW_GENERAL) {
                wordMeanChoices.clear();
                // 得到不是该单词的释义
                List<Interpretation> interpretationWrongs = LitePal.where("wordId != ?", WordController.currentWordId + "").find(Interpretation.class);
                Collections.shuffle(interpretationWrongs);

                if (recyclerView.getVisibility() == View.VISIBLE) {
                    wordMeanChoices.add(new ItemWordMeanChoice(WordController.currentWordId, stringBuilder.toString(), ItemWordMeanChoice.NOTSTART));
                    // 再添加3个随机意思
                    for (int i = 0; i < 3; ++i) {
                        wordMeanChoices.add(new ItemWordMeanChoice(-1, interpretationWrongs.get(i).getWordType() + ". " + interpretationWrongs.get(i).getCHSMeaning(), ItemWordMeanChoice.NOTSTART));
                    }
                    // 打乱顺序
                    Collections.shuffle(wordMeanChoices);
                    meanChoiceAdapter.notifyDataSetChanged();
                }

            }

            textLastWord.setText(lastWord);
            textLastWordMean.setText(lastWordMean);

            lastWord = words.get(0).getWord();
            lastWordMean = stringBuilder.toString();
        } else {
            Toast.makeText(this, "发生错误，请重试", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }

    }

    private void showLearn() {
        recyclerView.setVisibility(View.GONE);
        layoutBottomReview.setVisibility(View.GONE);
        layoutBottomLearn.setVisibility(View.VISIBLE);
    }

    private void showReview() {
        recyclerView.setVisibility(View.VISIBLE);
        layoutBottomReview.setVisibility(View.VISIBLE);
        layoutBottomLearn.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        needUpdate = true;
        long endTime = TimeController.getCurrentTimeStamp();
        long duration = endTime - startTime;
        startTime = -1;
        LearnTime learnTime = new LearnTime();
        List<LearnTime> learnTimeList = LitePal.where("date = ?", TimeController.getPastDateWithYear(0)).find(LearnTime.class);
        if (learnTimeList.isEmpty()) {
            learnTime.setTime(duration + "");
            learnTime.setDate(TimeController.getPastDateWithYear(0));
            learnTime.save();
        } else {
            int lastTime = Integer.valueOf(learnTimeList.get(0).getTime());
            learnTime.setTime((lastTime + duration) + "");
            learnTime.updateAll("date = ?", TimeController.getPastDateWithYear(0));
        }
    }

}
