package com.usts.englishlearning.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.usts.englishlearning.R;
import com.usts.englishlearning.config.ConfigData;
import com.usts.englishlearning.config.ConstantData;
import com.usts.englishlearning.database.MyDate;
import com.usts.englishlearning.database.UserConfig;
import com.usts.englishlearning.util.ActivityCollector;
import com.usts.englishlearning.util.FileUtil;
import com.usts.englishlearning.util.JsonHelper;

import org.litepal.LitePal;

import java.util.Calendar;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChangePlanActivity extends BaseActivity {

    private EditText editText;

    private TextView textGo, textBook, textWordMaxNum;

    private int maxNum;

    private static final String TAG = "ChangePlanActivity";

    private Thread thread;

    private int currentBookId;

    private List<UserConfig> userConfigs;

    private final int FINISH = 1;
    private final int DOWN_DONE = 2;

    private ProgressDialog progressDialog;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case FINISH:
                    // 等待框消失
                    progressDialog.dismiss();
                    // 重置上次学习时间
                    UserConfig userConfig1 = new UserConfig();
                    userConfig1.setLastStartTime(0);
                    userConfig1.setCurrentBookId(currentBookId);
                    userConfig1.updateAll("userId = ?", ConfigData.getSinaNumLogged() + "");
                    // 删除当天打卡记录
                    Calendar calendar = Calendar.getInstance();
                    LitePal.deleteAll(MyDate.class, "year = ? and month = ? and date = ? and userId = ?"
                            , calendar.get(Calendar.YEAR) + ""
                            , (calendar.get(Calendar.MONTH) + 1) + ""
                            , calendar.get(Calendar.DAY_OF_MONTH) + ""
                            , ConfigData.getSinaNumLogged() + "");
                    ActivityCollector.startOtherActivity(ChangePlanActivity.this, MainActivity.class);
                    break;
                case DOWN_DONE:
                    progressDialog.setMessage("已下载完成，正在解压分析数据中...");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_plan);

        init();

        Log.d(TAG, "onCreate: ");

        final Intent intent = getIntent();

        userConfigs = LitePal.where("userId = ?", ConfigData.getSinaNumLogged() + "").find(UserConfig.class);

        if (userConfigs.get(0).getWordNeedReciteNum() != 0)
            editText.setText(userConfigs.get(0).getWordNeedReciteNum() + "");

        textGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editText.getText().toString().trim().equals("")) {
                    if (Integer.parseInt(editText.getText().toString().trim()) >= 5
                            && Integer.parseInt(editText.getText().toString().trim()) < maxNum) {
                        // 隐藏软键盘
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                        // 设置数据
                        // 更新数据
                        final UserConfig userConfig = new UserConfig();
                        userConfig.setWordNeedReciteNum(Integer.parseInt(editText.getText().toString().trim()));
                        userConfig.updateAll("userId = ?", ConfigData.getSinaNumLogged() + "");
                        // 是第一次设置数据
                        if (ConfigData.notUpdate == intent.getIntExtra(ConfigData.UPDATE_NAME, 0)) {
                            // 开启等待框
                            progressDialog = new ProgressDialog(ChangePlanActivity.this);
                            progressDialog.setTitle("请稍等");
                            progressDialog.setMessage("数据包正在下载中...");
                            progressDialog.setCancelable(false);
                            progressDialog.show();
                            // 延迟两秒再运行，防止等待框不显示
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // 开启线程分析数据
                                    thread = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                OkHttpClient client = new OkHttpClient();
                                                Request request = new Request.Builder()
                                                        .url(ConstantData.bookDownLoadAddressById(currentBookId))
                                                        .build();
                                                Response response = client.newCall(request).execute();
                                                Message message = new Message();
                                                message.what = DOWN_DONE;
                                                handler.sendMessage(message);
                                                FileUtil.getFileByBytes(response.body().bytes(), getFilesDir()+ "/" + ConstantData.DIR_TOTAL, ConstantData.bookFileNameById(currentBookId));
                                                FileUtil.unZipFile(getFilesDir()+ "/" + ConstantData.DIR_TOTAL + "/" + ConstantData.bookFileNameById(currentBookId)
                                                        , getFilesDir() + "/" + ConstantData.DIR_TOTAL + "/" + ConstantData.DIR_AFTER_FINISH, false);
                                            } catch (Exception e) {

                                            }
                                            JsonHelper.analyseDefaultAndSave(FileUtil.readLocalJson(ConstantData.DIR_TOTAL + "/" + ConstantData.DIR_AFTER_FINISH + "/" + ConstantData.bookFileNameById(currentBookId).replace(".zip", ".json")));
                                            Message message = new Message();
                                            message.what = FINISH;
                                            handler.sendMessage(message);
                                        }
                                    });
                                    thread.start();
                                }
                            }, 500);
                        } else {
                            if (userConfigs.get(0).getWordNeedReciteNum() != Integer.parseInt(editText.getText().toString().trim())) {
                                // 重置上次学习时间
                                UserConfig userConfig1 = new UserConfig();
                                userConfig1.setLastStartTime(-1);
                                userConfig1.updateAll("userId = ?", ConfigData.getSinaNumLogged() + "");
                                Toast.makeText(ChangePlanActivity.this, "" + LitePal.where("userId = ?", ConfigData.getSinaNumLogged() + "").find(UserConfig.class).get(0).getLastStartTime(), Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "onClick: " + LitePal.where("userId = ?", ConfigData.getSinaNumLogged() + "").find(UserConfig.class).get(0).getLastStartTime());
                                // 删除当天打卡记录
                                Calendar calendar = Calendar.getInstance();
                                LitePal.deleteAll(MyDate.class, "year = ? and month = ? and date = ? and userId = ?"
                                        , calendar.get(Calendar.YEAR) + ""
                                        , (calendar.get(Calendar.MONTH) + 1) + ""
                                        , calendar.get(Calendar.DAY_OF_MONTH) + ""
                                        , ConfigData.getSinaNumLogged() + "");
                                Toast.makeText(ChangePlanActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ChangePlanActivity.this, "计划未变", Toast.LENGTH_SHORT).show();
                            }
                            ActivityCollector.startOtherActivity(ChangePlanActivity.this, MainActivity.class);
                        }
                    } else {
                        Toast.makeText(ChangePlanActivity.this, "请输入合理的范围", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ChangePlanActivity.this, "请输入值再继续", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void init() {
        editText = findViewById(R.id.edit_word_num);
        textGo = findViewById(R.id.text_plan_next);
        textBook = findViewById(R.id.text_plan_chosen);
        textWordMaxNum = findViewById(R.id.text_max_word_num);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        // 获得数据
        List<UserConfig> userConfigs = LitePal.where("userId = ?", ConfigData.getSinaNumLogged() + "").find(UserConfig.class);
        currentBookId = userConfigs.get(0).getCurrentBookId();

        maxNum = ConstantData.wordTotalNumberById(currentBookId);

        // 设置最大背单词量
        textWordMaxNum.setText(maxNum + "");

        // 设置书名
        textBook.setText(ConstantData.bookNameById(currentBookId));

    }

    @Override
    public void onBackPressed() {
        List<UserConfig> userConfigs = LitePal.where("userId = ?", ConfigData.getSinaNumLogged() + "").find(UserConfig.class);
        if (userConfigs.get(0).getWordNeedReciteNum() != 0)
            super.onBackPressed();
        else {
            ActivityCollector.startOtherActivity(ChangePlanActivity.this, ChooseWordDBActivity.class);
        }
    }
}
