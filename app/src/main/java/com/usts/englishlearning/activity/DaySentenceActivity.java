package com.usts.englishlearning.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.gyf.immersionbar.ImmersionBar;
import com.usts.englishlearning.R;
import com.usts.englishlearning.database.DailyData;
import com.usts.englishlearning.util.MediaHelper;
import com.usts.englishlearning.util.TimeController;

import org.litepal.LitePal;

import java.util.Calendar;
import java.util.List;

public class DaySentenceActivity extends BaseActivity {

    private LinearLayout linearLayout;

    private ImageView imgBg, imgSound, imgShare, imgExit;

    private TextView textDate, textMonth, textYear, textSentenceEn, textSentenceCn;

    private final int FINISH = 1;

    private AlphaAnimation startAnimation, exitAnimation;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case FINISH:
                    List<DailyData> dailyDataList = LitePal.where("dayTime = ?", TimeController.getCurrentDateStamp() + "").find(DailyData.class);
                    if (!dailyDataList.isEmpty()) {
                        final DailyData dailyData = dailyDataList.get(0);
                        Glide.with(DaySentenceActivity.this).load(dailyData.getPicVertical()).into(imgBg);
                        // 设置日期
                        Calendar calendar = Calendar.getInstance();
                        textDate.setText(calendar.get(Calendar.DATE) + "");
                        textYear.setText(calendar.get(Calendar.YEAR) + "");
                        textMonth.setText(getMonthName(calendar));
                        textSentenceEn.setText(dailyData.getDailyEn());
                        textSentenceCn.setText(dailyData.getDailyChs());
                        linearLayout.startAnimation(startAnimation);
                        if (dailyData.getDailySound() != null)
                            imgSound.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    MediaHelper.playInternetSource(dailyData.getDailySound());
                                }
                            });
                        imgShare.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent shareIntent = new Intent();
                                shareIntent.setAction(Intent.ACTION_SEND);
                                shareIntent.setType("text/plain");
                                shareIntent.putExtra(Intent.EXTRA_TEXT, dailyData.getDailyEn() + "\n" + dailyData.getDailyChs());
                                shareIntent = Intent.createChooser(shareIntent, "每日一句");
                                startActivity(shareIntent);
                            }
                        });
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_sentence);

        //windowSlide(Gravity.TOP);

        // 设置浅色字体
        ImmersionBar.with(this).statusBarDarkFont(false).init();

        init();

        // 递进动画
        exitAnimation = new AlphaAnimation(1.0f, 0.0f);
        exitAnimation.setDuration(100);
        exitAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                linearLayout.setVisibility(View.GONE);
                imgShare.setVisibility(View.GONE);
                imgExit.setVisibility(View.GONE);
                supportFinishAfterTransition();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        startAnimation = new AlphaAnimation(0.0f, 1.0f);
        startAnimation.setDuration(2000);

        // 设置数据显示
        new Thread(new Runnable() {
            @Override
            public void run() {
                prepareDailyData();
                Message message = new Message();
                message.what = FINISH;
                handler.sendMessage(message);
            }
        }).start();

    }

    // 初始化
    private void init() {
        linearLayout = findViewById(R.id.layout_day_sen_content);
        imgBg = findViewById(R.id.img_ds_show);
        textDate = findViewById(R.id.text_ds_date);
        textMonth = findViewById(R.id.text_ds_month);
        textYear = findViewById(R.id.text_ds_year);
        textSentenceCn = findViewById(R.id.text_sentence_cn);
        textSentenceEn = findViewById(R.id.text_sentence_en);
        imgSound = findViewById(R.id.img_ds_sound);
        imgShare = findViewById(R.id.img_ds_share);
        imgExit = findViewById(R.id.img_ds_exit);
    }

    // 设置获得月份缩写
    public static String getMonthName(Calendar calendar) {
        String s = "";
        switch (calendar.get(Calendar.MONTH)) {
            case 0:
                s = "Jan";
                break;
            case 1:
                s = "Feb";
                break;
            case 2:
                s = "Mar";
                break;
            case 3:
                s = "Apr";
                break;
            case 4:
                s = "May";
                break;
            case 5:
                s = "June";
                break;
            case 6:
                s = "July";
                break;
            case 7:
                s = "Aug";
                break;
            case 8:
                s = "Sep";
                break;
            case 9:
                s = "Oct";
                break;
            case 10:
                s = "Nov";
                break;
            case 11:
                s = "Dec";
                break;
        }
        return s;
    }

    @Override
    public void onBackPressed() {
        linearLayout.startAnimation(exitAnimation);
    }
}
