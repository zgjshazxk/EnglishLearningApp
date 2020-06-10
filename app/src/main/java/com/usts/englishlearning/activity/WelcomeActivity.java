package com.usts.englishlearning.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.usts.englishlearning.R;
import com.usts.englishlearning.activity.service.NotifyLearnService;
import com.usts.englishlearning.config.ConfigData;
import com.usts.englishlearning.config.ServerData;
import com.usts.englishlearning.database.DailyData;
import com.usts.englishlearning.database.User;
import com.usts.englishlearning.database.UserConfig;
import com.usts.englishlearning.listener.PermissionListener;
import com.usts.englishlearning.util.ActivityCollector;
import com.usts.englishlearning.util.BaiduHelper;
import com.usts.englishlearning.util.MyApplication;
import com.usts.englishlearning.util.MyPopWindow;
import com.usts.englishlearning.util.TimeController;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WelcomeActivity extends BaseActivity implements View.OnClickListener {

    // 壁纸
    private ImageView imgBackground;
    // 每日一句卡片
    private CardView cardWelCome;
    // 每日一句文字
    private TextView textWelCome;
    // 弹出-同意按钮
    private CardView cardAgree;
    // 弹出-不同意按钮
    private TextView textNotAgree;
    // 弹出视图
    private MyPopWindow welWindow;
    // 缩放动画
    private ScaleAnimation animation;

    private static final String TAG = "WelcomeActivity";

    private final int FINISH = 1;

    private String rootPath;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case FINISH:
                    List<DailyData> dailyDataList = LitePal.where("dayTime = ?", TimeController.getCurrentDateStamp() + "").find(DailyData.class);
                    if (!dailyDataList.isEmpty()) {
                        DailyData dailyData = dailyDataList.get(0);
                        textWelCome.setText(dailyData.getDailyEn());
                        Glide.with(WelcomeActivity.this).load(dailyData.getPicVertical()).into(imgBackground);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        rootPath = Environment.getDataDirectory().getPath();
        Log.d(TAG, "路径" + rootPath);

        // 防止重复
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            ActivityCollector.removeActivity(this);
            finish();
            return;
        }

        init();

        // 设置透明度
        cardWelCome.getBackground().setAlpha(200);

        // 设置数据显示
        new Thread(new Runnable() {
            @Override
            public void run() {
                prepareDailyData();
                Message message = new Message();
                message.what = FINISH;
                handler.sendMessage(message);
                BaiduHelper.getAssessToken();
            }
        }).start();

        // 如果是第一次运行
        if (ConfigData.getIsFirst()) {
            welWindow.setClipChildren(false)
                    .setBlurBackgroundEnable(true)
                    .setOutSideDismiss(false)
                    .showPopupWindow();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    imgBackground.startAnimation(animation);
                }
            }, 500);
            // 设置学习提醒
            if (ConfigData.getIsAlarm()) {
                int hour = Integer.parseInt(ConfigData.getAlarmTime().split("-")[0]);
                int minute = Integer.parseInt(ConfigData.getAlarmTime().split("-")[1]);
                AlarmActivity.startAlarm(hour, minute, false, false);
            }
            // 设置通知栏单词
            if (ConfigData.getIsNotifyLearn()) {
                if (!BaseActivity.isServiceExisted(MyApplication.getContext(), NotifyLearnService.class.getName())) {
                    // 检查当前是否数据有效
                    LearnInNotifyActivity.checkIsAvailable();
                    LearnInNotifyActivity.startService(ConfigData.getNotifyLearnMode());
                }
            }
        }

        MainActivity.lastFragment = 0;
        MainActivity.needRefresh = true;

    }

    private void init() {
        // 设置权限弹出框
        welWindow = new MyPopWindow(this);
        cardWelCome = findViewById(R.id.card_wel1);
        textWelCome = findViewById(R.id.text_wel);
        imgBackground = findViewById(R.id.img_wel_bg);
        cardAgree = welWindow.findViewById(R.id.card_agree);
        cardAgree.setOnClickListener(this);
        textNotAgree = welWindow.findViewById(R.id.text_not_agree);
        textNotAgree.setOnClickListener(this);
        animationConfig();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.card_agree:
                requestPermission();
                break;
            case R.id.text_not_agree:
                Toast.makeText(this, "抱歉，程序即将退出", Toast.LENGTH_SHORT).show();
                ActivityCollector.finishAll();
                break;
        }
    }

    // 权限管理
    private void requestPermission() {
        requestRunPermission(ConfigData.permissions, new PermissionListener() {
            @Override
            public void onGranted() {
                welWindow.dismiss();
                // 设置第一次运行的值为否
                ConfigData.setIsFirst(false);
                // 延迟时间再开始动画
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        imgBackground.startAnimation(animation);
                    }
                }, MyPopWindow.animatTime);
            }

            @Override
            public void onDenied(List<String> deniedPermission) {
                if (!deniedPermission.isEmpty()) {
                    Toast.makeText(WelcomeActivity.this, "无法获得权限，程序即将退出", Toast.LENGTH_SHORT).show();
                    ActivityCollector.finishAll();
                }
            }
        });
    }

    // 缩放动画配置
    private void animationConfig() {
        // 从原图大小，放大到1.5倍
        animation = new ScaleAnimation(1, 1.5f, 1, 1.5f, Animation.RELATIVE_TO_SELF, 0.5f, 1, 0.5f);
        // 设置持续时间
        animation.setDuration(4000);
        // 设置动画结束之后的状态是否是动画的最终状态
        animation.setFillAfter(true);
        // 设置循环次数
        animation.setRepeatCount(0);
        // 设置动画结束后事件
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // 已登录，进入首页/选择词书
                if (ConfigData.getIsLogged()) {
                    List<UserConfig> userConfigs = LitePal.where("userId = ?", ConfigData.getSinaNumLogged() + "").find(UserConfig.class);
                    if (userConfigs.get(0).getCurrentBookId() == -1) {
                        Intent intent = new Intent(WelcomeActivity.this, ChooseWordDBActivity.class);
                        startActivity(intent);
                    } else if (userConfigs.get(0).getCurrentBookId() != -1 && userConfigs.get(0).getWordNeedReciteNum() == 0) {
                        Intent intent = new Intent(WelcomeActivity.this, ChangePlanActivity.class);
                        startActivity(intent);
                    } else {

                        // 后台更新登录时间
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                updateServerData();
                            }
                        }).start();
                        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }
                // 未登录，进入登录页面
                else {
                    Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void updateServerData() {
        List<User> users = LitePal.where("userId = ?", ConfigData.getSinaNumLogged() + "").find(User.class);
        RequestBody formBody = new FormBody.Builder()
                .add(ServerData.LOGIN_SINA_NUM, users.get(0).getUserId()+"")
                .add(ServerData.LOGIN_SINA_NAME, users.get(0).getUserName())
                .build();
        Request request = new Request.Builder()
                .post(formBody)
                .url(ServerData.SERVER_LOGIN_ADDRESS)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                    }
                });
    }

}
