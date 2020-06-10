package com.usts.englishlearning.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.lihang.ShadowLayout;
import com.sina.weibo.sdk.WbSdk;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbAuthListener;
import com.sina.weibo.sdk.auth.WbConnectErrorMessage;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.usts.englishlearning.R;
import com.usts.englishlearning.config.ConfigData;
import com.usts.englishlearning.config.ServerData;
import com.usts.englishlearning.config.SinaData;
import com.usts.englishlearning.database.User;
import com.usts.englishlearning.database.UserConfig;
import com.usts.englishlearning.object.JsonSina;
import com.usts.englishlearning.util.ActivityCollector;

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

public class LoginActivity extends BaseActivity {

    private ImageView imgPic;

    // 登录按钮
    private ShadowLayout cardLogin;

    private LinearLayout linearLayout;

    private static final String TAG = "LoginActivity";

    private final int SUCCESS = 1;
    private final int FAILED = 2;

    private ProgressDialog progressDialog;

    private SsoHandler ssoHandler;

    private String content;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case FAILED:
                    Toast.makeText(LoginActivity.this, "登录失败，请检查服务器与网络状态", Toast.LENGTH_SHORT).show();
                    break;
                case SUCCESS:
                    ActivityCollector.startOtherActivity(LoginActivity.this, ChooseWordDBActivity.class);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();

        Glide.with(this).load(R.drawable.pic_learning).into(imgPic);

        // 渐变动画
        AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(2000);
        imgPic.startAnimation(animation);

        cardLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("提示")
                        .setMessage("本软件仅收集用户名、ID、头像三个必要的信息，我们不会泄露您的个人隐私，仅作为标识使用。请放心使用")
                        .setPositiveButton("继续", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                initSinaLogin();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();


            }
        });

    }

    private void init() {
        imgPic = findViewById(R.id.img_inbetweening);
        cardLogin = findViewById(R.id.card_sina_login);
        linearLayout = findViewById(R.id.linear_login);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("提示")
                .setMessage("确定要退出吗?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCollector.finishAll();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void initSinaLogin() {
        WbSdk.install(this, new AuthInfo(this, SinaData.APP_KEY, SinaData.REDIRECT_URL, SinaData.SCOPE));
        ssoHandler = new SsoHandler(LoginActivity.this);
        ssoHandler.authorize(new WbAuthListener() {
            @Override
            public void onSuccess(Oauth2AccessToken oauth2AccessToken) {
                Toast.makeText(LoginActivity.this, "授权成功！", Toast.LENGTH_SHORT).show();
                // 请求用户数据
                Request request = new Request.Builder()
                        .url("https://api.weibo.com/2/users/show.json?access_token=" + oauth2AccessToken.getToken() + "&uid=" + oauth2AccessToken.getUid())
                        .build();

                OkHttpClient okHttpClient = new OkHttpClient();
                okHttpClient.newCall(request)
                        .enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Message message = new Message();
                                message.what = FAILED;
                                handler.sendMessage(message);
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                content = response.body().string();
                                Gson gson = new Gson();
                                final JsonSina jsonSina = gson.fromJson(content, JsonSina.class);

                                List<User> users = LitePal.where("userId = ?", jsonSina.getId() + "").find(User.class);
                                if (users.isEmpty()) {
                                    User user = new User();
                                    user.setUserName(jsonSina.getName());
                                    user.setUserProfile(jsonSina.getProfile_image_url());
                                    user.setUserId(jsonSina.getId());
                                    // 测试
                                    user.setUserMoney(0);
                                    user.setUserWordNumber(0);
                                    user.save();
                                }
                                // 查询在用户配置表中，是否存在该用户，若没有，则新建数据
                                List<UserConfig> userConfigs = LitePal.where("userId = ?", jsonSina.getId() + "").find(UserConfig.class);
                                if (userConfigs.isEmpty()) {
                                    UserConfig userConfig = new UserConfig();
                                    userConfig.setUserId(jsonSina.getId());
                                    userConfig.setCurrentBookId(-1);
                                    userConfig.save();
                                }
                                // 默认已登录并设置已登录的微博ID
                                ConfigData.setIsLogged(true);
                                ConfigData.setSinaNumLogged(jsonSina.getId());

                                Message message = new Message();
                                message.what = SUCCESS;
                                handler.sendMessage(message);

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        RequestBody formBody = new FormBody.Builder()
                                                .add(ServerData.LOGIN_SINA_NUM, jsonSina.getId() + "")
                                                .add(ServerData.LOGIN_SINA_NAME, jsonSina.getName())
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
                                }).start();

                            }
                        });
            }

            @Override
            public void cancel() {
                Message message = new Message();
                message.what = FAILED;
                handler.sendMessage(message);
            }

            @Override
            public void onFailure(WbConnectErrorMessage wbConnectErrorMessage) {
                Message message = new Message();
                message.what = FAILED;
                handler.sendMessage(message);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (ssoHandler != null) {
            ssoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }
}


