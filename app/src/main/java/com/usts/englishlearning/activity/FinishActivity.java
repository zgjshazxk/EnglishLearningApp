package com.usts.englishlearning.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.usts.englishlearning.R;
import com.usts.englishlearning.config.ConfigData;
import com.usts.englishlearning.database.MyDate;
import com.usts.englishlearning.database.User;
import com.usts.englishlearning.database.UserConfig;
import com.usts.englishlearning.util.ActivityCollector;
import com.usts.englishlearning.util.TimeController;
import com.usts.englishlearning.util.WordController;

import org.litepal.LitePal;

import java.util.Calendar;
import java.util.List;

public class FinishActivity extends BaseActivity {

    private TextView textWord, textDay;

    private EditText editRemark;

    private Button btnBack;

    private int wordNum;

    private List<UserConfig> userConfigs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);

        init();

        windowExplode();

        userConfigs = LitePal.where("userId = ?", ConfigData.getSinaNumLogged() + "").find(UserConfig.class);

        wordNum = userConfigs.get(0).getWordNeedReciteNum() + WordController.wordReviewNum;
        textWord.setText(wordNum + "");
        List<MyDate> myDateList = LitePal.findAll(MyDate.class);
        textDay.setText((myDateList.size() + 1) + "");

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private void init() {
        textWord = findViewById(R.id.text_fi_word_num);
        textDay = findViewById(R.id.text_fi_days);
        editRemark = findViewById(R.id.edit_fi_remark);
        btnBack = findViewById(R.id.btn_fi_back);
    }

    @Override
    public void onBackPressed() {
        saveData();
        ActivityCollector.startOtherActivity(FinishActivity.this, MainActivity.class);
    }

    private void saveData() {
        Calendar calendar = Calendar.getInstance();
        List<MyDate> myDates = LitePal.where("year = ? and month = ? and date = ? and userId = ?",
                calendar.get(Calendar.YEAR) + "",
                (calendar.get(Calendar.MONTH) + 1) + "",
                calendar.get(Calendar.DATE) + "",
                ConfigData.getSinaNumLogged() + "").find(MyDate.class);
        if (myDates.isEmpty()) {
            dataControl();
        } else {
            int result = LitePal.deleteAll("year = ? and month = ? and date = ? and userId = ?",
                    calendar.get(Calendar.YEAR) + "",
                    (calendar.get(Calendar.MONTH) + 1) + "",
                    calendar.get(Calendar.DATE) + "",
                    ConfigData.getSinaNumLogged() + "");
            if (result != 0) {
                dataControl();
            }
        }
    }

    private void dataControl() {
        String[] s = TimeController.getStringDate(TimeController.todayDate).split("-");
        MyDate myDate = new MyDate();
        myDate.setWordLearnNumber(userConfigs.get(0).getWordNeedReciteNum());
        myDate.setWordReviewNumber(WordController.wordReviewNum);
        myDate.setYear(Integer.valueOf(s[0]));
        myDate.setMonth(Integer.valueOf(s[1]));
        myDate.setDate(Integer.valueOf(s[2]));
        myDate.setUserId(ConfigData.getSinaNumLogged());
        if (!editRemark.getText().toString().trim().isEmpty())
            myDate.setRemark(editRemark.getText().toString());
        myDate.save();
        // 增加10金币
        List<User> users = LitePal.where("userId = ?", ConfigData.getSinaNumLogged() + "").find(User.class);
        User user = new User();
        user.setUserMoney(users.get(0).getUserMoney() + 10);
        user.setUserWordNumber(users.get(0).getUserWordNumber() + userConfigs.get(0).getWordNeedReciteNum());
        user.updateAll("userId = ?", ConfigData.getSinaNumLogged() + "");
    }

}
