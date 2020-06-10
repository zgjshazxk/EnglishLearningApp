package com.usts.englishlearning.activity.index;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;

import com.usts.englishlearning.R;
import com.usts.englishlearning.activity.BaseActivity;
import com.usts.englishlearning.activity.DaySentenceActivity;
import com.usts.englishlearning.activity.MainActivity;
import com.usts.englishlearning.activity.SearchActivity;
import com.usts.englishlearning.activity.WordDetailActivity;
import com.usts.englishlearning.activity.WordFolderActivity;
import com.usts.englishlearning.activity.load.LoadWordActivity;
import com.usts.englishlearning.config.ConfigData;
import com.usts.englishlearning.config.ConstantData;
import com.usts.englishlearning.database.Interpretation;
import com.usts.englishlearning.database.MyDate;
import com.usts.englishlearning.database.UserConfig;
import com.usts.englishlearning.database.Word;
import com.usts.englishlearning.util.NumberController;

import org.litepal.LitePal;

import java.util.Calendar;
import java.util.List;

public class FragmentWord extends Fragment implements View.OnClickListener {

    private CardView cardStart, cardSearch;
    private ImageView imgRefresh, imgSearch, imgFlag;
    private View tranView, tranSearchView;
    private TextView textStart;
    private RelativeLayout layoutFiles;

    private TextView textWord, textMean, textWordNum, textBook;

    private TextView textDate, textMonth;

    private static final String TAG = "FragmentWord";

    private int currentBookId;

    private boolean isOnClick = true;

    private int currentRandomId;

    public static int prepareData = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_word, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        init();

        Log.d(TAG, "onActivityCreated: ");

        if (MainActivity.needRefresh) {
            prepareData = 0;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    BaseActivity.prepareDailyData();
                }
            }).start();
        }

    }

    // 初始化控件
    private void init() {
        imgRefresh = getActivity().findViewById(R.id.img_refresh);
        imgRefresh.setOnClickListener(this);
        cardStart = getActivity().findViewById(R.id.card_index_start);
        cardStart.setOnClickListener(this);
        tranView = getActivity().findViewById(R.id.view_main_tran);
        textMean = getActivity().findViewById(R.id.text_main_show_word_mean);
        textMean.setOnClickListener(this);
        textWord = getActivity().findViewById(R.id.text_main_show_word);
        textWordNum = getActivity().findViewById(R.id.text_main_show_word_num);
        textBook = getActivity().findViewById(R.id.text_main_show_book_name);
        textStart = getActivity().findViewById(R.id.text_main_start);
        textStart.setOnClickListener(this);
        layoutFiles = getActivity().findViewById(R.id.layout_main_words);
        layoutFiles.setOnClickListener(this);
        textDate = getActivity().findViewById(R.id.text_main_date);
        textMonth = getActivity().findViewById(R.id.text_main_month);
        cardSearch = getActivity().findViewById(R.id.card_main_search);
        cardSearch.setOnClickListener(this);
        imgSearch = getActivity().findViewById(R.id.img_review_search);
        imgFlag = getActivity().findViewById(R.id.img_top_flag);
        imgFlag.setOnClickListener(this);
        tranSearchView = getActivity().findViewById(R.id.view_search_tran);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_refresh:
                // 旋转动画
                RotateAnimation animation = new RotateAnimation(0.0f, 360.0f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                animation.setDuration(700);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        setRandomWord();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imgRefresh.startAnimation(animation);
                break;
            case R.id.text_main_start:
                if (isOnClick) {
                    Intent mIntent = new Intent(getActivity(), LoadWordActivity.class);
                    mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(mIntent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                    isOnClick = false;
                }
                break;
            case R.id.img_top_flag:
                Intent mIntent = new Intent(getActivity(), DaySentenceActivity.class);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                        tranView, "mainTrans");
                startActivity(mIntent, activityOptionsCompat.toBundle());
                break;
            case R.id.text_main_show_word_mean:
                WordDetailActivity.wordId = currentRandomId;
                Intent intent = new Intent(getActivity(), WordDetailActivity.class);
                intent.putExtra(WordDetailActivity.TYPE_NAME, WordDetailActivity.TYPE_GENERAL);
                startActivity(intent);
                break;
            case R.id.card_main_search:
                Intent intent2 = new Intent(getActivity(), SearchActivity.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ActivityOptionsCompat activityOptionsCompat2 = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                        tranSearchView, "imgSearch");
                startActivity(intent2, activityOptionsCompat2.toBundle());
                break;
            case R.id.layout_main_words:
                Intent intent3 = new Intent(getActivity(), WordFolderActivity.class);
                startActivity(intent3, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void setRandomWord() {
        ++prepareData;
        Log.d(TAG, "setRandomWord: " + ConstantData.wordTotalNumberById(currentBookId));
        int randomId = NumberController.getRandomNumber(1, ConstantData.wordTotalNumberById(currentBookId));
        Log.d(TAG, "当前ID" + randomId);
        currentRandomId = randomId;
        Log.d(TAG, "要传入的ID" + currentRandomId);
        Log.d(TAG, randomId + "");
        Word word = LitePal.where("wordId = ?", randomId + "").select("wordId", "word").find(Word.class).get(0);
        Log.d(TAG, word.getWord());
        List<Interpretation> interpretations = LitePal.where("wordId = ?", word.getWordId() + "").find(Interpretation.class);
        textWord.setText(word.getWord());
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < interpretations.size(); ++i) {
            stringBuilder.append(interpretations.get(i).getWordType() + ". " + interpretations.get(i).getCHSMeaning());
            if (i != interpretations.size() - 1)
                stringBuilder.append("\n");
        }
        textMean.setText(stringBuilder.toString());
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        Calendar calendar = Calendar.getInstance();
        textDate.setText(calendar.get(Calendar.DATE) + "");
        textMonth.setText(DaySentenceActivity.getMonthName(calendar));
        List<Word> words = LitePal.where("deepMasterTimes <> ?", 3 + "").select("wordId").find(Word.class);
        List<MyDate> myDates = LitePal.where("year = ? and month = ? and date = ? and userId = ?",
                calendar.get(Calendar.YEAR) + "",
                (calendar.get(Calendar.MONTH) + 1) + "",
                calendar.get(Calendar.DATE) + "",
                ConfigData.getSinaNumLogged() + "").find(MyDate.class);
        if (!words.isEmpty()) {
            if (myDates.isEmpty()) {
                // 未完成计划
                cardStart.setCardBackgroundColor(getActivity().getColor(R.color.colorLightBlue));
                textStart.setTextColor(getActivity().getColor(R.color.colorFontInBlue));
                textStart.setText("开始背单词");
                isOnClick = true;
            } else {
                // 完成计划
                if ((myDates.get(0).getWordLearnNumber() + myDates.get(0).getWordReviewNumber()) > 0) {
                    cardStart.setCardBackgroundColor(getActivity().getColor(R.color.colorBgWhite));
                    textStart.setTextColor(getActivity().getColor(R.color.colorFontInWhite));
                    textStart.setText("已完成今日任务");
                    cardStart.setClickable(false);
                    isOnClick = false;
                } else {
                    // 未完成计划
                    cardStart.setCardBackgroundColor(getActivity().getColor(R.color.colorLightBlue));
                    textStart.setTextColor(getActivity().getColor(R.color.colorFontInBlue));
                    textStart.setText("开始背单词");
                    isOnClick = true;
                }
            }
        } else {
            cardStart.setCardBackgroundColor(getActivity().getColor(R.color.colorBgWhite));
            textStart.setTextColor(getActivity().getColor(R.color.colorFontInWhite));
            textStart.setText("恭喜！已背完此书");
            cardStart.setClickable(false);
            isOnClick = false;
        }
        // 设置界面数据
        List<UserConfig> userConfigs = LitePal.where("userId = ?", ConfigData.getSinaNumLogged() + "").find(UserConfig.class);
        currentBookId = userConfigs.get(0).getCurrentBookId();
        textWordNum.setText("每日须学" + userConfigs.get(0).getWordNeedReciteNum() + "个单词");
        textBook.setText(ConstantData.bookNameById(currentBookId));
        if (prepareData == 0)
            // 设置随机数据
            setRandomWord();
    }
}
