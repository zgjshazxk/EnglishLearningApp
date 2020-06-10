package com.usts.englishlearning.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.usts.englishlearning.R;
import com.usts.englishlearning.activity.service.NotifyLearnService;
import com.usts.englishlearning.config.ConfigData;
import com.usts.englishlearning.config.ConstantData;
import com.usts.englishlearning.database.Word;
import com.usts.englishlearning.util.MyApplication;

import org.litepal.LitePal;

public class LearnInNotifyActivity extends BaseActivity {

    private Switch aSwitch;

    private RelativeLayout layoutSort;

    private TextView textSort;

    private String[] sorts = {"全部单词", "收藏单词", "已学单词", "随机单词"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_in_notify);

        init();

        if (ConfigData.getIsNotifyLearn()) {
            aSwitch.setChecked(true);
            layoutSort.setVisibility(View.VISIBLE);
            // 检查当前是否数据有效
            checkIsAvailable();
            if (!BaseActivity.isServiceExisted(MyApplication.getContext(), NotifyLearnService.class.getName())) {
                startService(ConfigData.getNotifyLearnMode());
            }
        } else {
            aSwitch.setChecked(false);
            layoutSort.setVisibility(View.GONE);
        }

        textSort.setText(sorts[ConfigData.getNotifyLearnMode()]);

        layoutSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(LearnInNotifyActivity.this);
                builder.setSingleChoiceItems(sorts, ConfigData.getNotifyLearnMode(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        // 延迟500毫秒取消对话框
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                // 更换模式
                                if (ConfigData.getNotifyLearnMode() != which) {
                                    switch (which) {
                                        case NotifyLearnService.ALL_MODE:
                                            startService(NotifyLearnService.ALL_MODE);
                                            Toast.makeText(LearnInNotifyActivity.this, "已开启通知", Toast.LENGTH_SHORT).show();
                                            textSort.setText(sorts[NotifyLearnService.ALL_MODE]);
                                            ConfigData.setNotifyLearnMode(NotifyLearnService.ALL_MODE);
                                            break;
                                        case NotifyLearnService.STAR_MODE:
                                            if (!LitePal.where("isCollected = ?", 1 + "").find(Word.class).isEmpty()) {
                                                startService(NotifyLearnService.STAR_MODE);
                                                Toast.makeText(LearnInNotifyActivity.this, "已开启通知", Toast.LENGTH_SHORT).show();
                                                textSort.setText(sorts[NotifyLearnService.STAR_MODE]);
                                                ConfigData.setNotifyLearnMode(NotifyLearnService.STAR_MODE);
                                            } else
                                                Toast.makeText(LearnInNotifyActivity.this, "抱歉，你还有收藏过单词", Toast.LENGTH_SHORT).show();
                                            break;
                                        case NotifyLearnService.LEARN_MODE:
                                            if (!LitePal.where("isLearned = ?", 1 + "").find(Word.class).isEmpty()) {
                                                startService(NotifyLearnService.LEARN_MODE);
                                                Toast.makeText(LearnInNotifyActivity.this, "已开启通知", Toast.LENGTH_SHORT).show();
                                                textSort.setText(sorts[NotifyLearnService.LEARN_MODE]);
                                                ConfigData.setNotifyLearnMode(NotifyLearnService.LEARN_MODE);
                                            } else
                                                Toast.makeText(LearnInNotifyActivity.this, "抱歉，你还有学习过单词", Toast.LENGTH_SHORT).show();
                                            break;
                                        case NotifyLearnService.RANDOM_MODE:
                                            startService(NotifyLearnService.RANDOM_MODE);
                                            Toast.makeText(LearnInNotifyActivity.this, "已开启通知", Toast.LENGTH_SHORT).show();
                                            textSort.setText(sorts[NotifyLearnService.RANDOM_MODE]);
                                            ConfigData.setNotifyLearnMode(NotifyLearnService.RANDOM_MODE);
                                            break;
                                    }
                                }
                            }
                        }, 200);
                    }
                }).show();
            }
        });

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ConfigData.setIsNotifyLearn(true);
                    layoutSort.setVisibility(View.VISIBLE);
                    checkIsAvailable();
                    startService(ConfigData.getNotifyLearnMode());
                } else {
                    ConfigData.setIsNotifyLearn(false);
                    layoutSort.setVisibility(View.GONE);
                    stopService();
                }
            }
        });

    }

    private void init() {
        aSwitch = findViewById(R.id.switch_notify_learn);
        textSort = findViewById(R.id.text_notify_sort);
        layoutSort = findViewById(R.id.layout_notify_sort);
    }

    public static void startService(int mode) {
        NotifyLearnService.currentMode = mode;
        Intent intent = new Intent(MyApplication.getContext(), NotifyLearnService.class);
        if (BaseActivity.isServiceExisted(MyApplication.getContext(), NotifyLearnService.class.getName()))
            stopService();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            MyApplication.getContext().startForegroundService(intent);
        } else {
            MyApplication.getContext().startService(intent);
        }
    }

    public static void stopService() {
        Intent intent = new Intent(MyApplication.getContext(), NotifyLearnService.class);
        MyApplication.getContext().stopService(intent);
    }

    public static void checkIsAvailable() {
        switch (ConfigData.getNotifyLearnMode()) {
            case NotifyLearnService.LEARN_MODE:
                if (LitePal.where("isLearned = ?", 1 + "").find(Word.class).isEmpty())
                    ConfigData.setNotifyLearnMode(NotifyLearnService.ALL_MODE);
                break;
            case NotifyLearnService.STAR_MODE:
                if (LitePal.where("isCollected = ?", 1 + "").find(Word.class).isEmpty())
                    ConfigData.setNotifyLearnMode(NotifyLearnService.ALL_MODE);
                break;
        }
    }

}
