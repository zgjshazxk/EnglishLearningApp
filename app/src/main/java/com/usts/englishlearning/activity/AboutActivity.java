package com.usts.englishlearning.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import com.usts.englishlearning.R;
import com.usts.englishlearning.config.ConstantData;
import com.usts.englishlearning.util.MyApplication;
import com.usts.englishlearning.util.NumberController;

public class AboutActivity extends BaseActivity {

    private TextView textVersion, textName, textContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        init();

        textVersion.setText("当前版本：" + getAppVersionName(AboutActivity.this) + "（" + getAppVersionCode(AboutActivity.this) + "）");
        textContent.setText(ConstantData.phrases[NumberController.getRandomNumber(0, ConstantData.phrases.length - 1)]);
        textName.setText(getAppName(MyApplication.getContext()));


    }

    private void init() {
        textVersion = findViewById(R.id.text_about_version);
        textName = findViewById(R.id.text_about_name);
        textContent = findViewById(R.id.text_about_content);
    }

    public static String getAppVersionCode(Context context) {
        int versioncode = 0;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            // versionName = pi.versionName;
            versioncode = pi.versionCode;
        } catch (Exception e) {
            //Log.e("VersionInfo", "Exception", e);
        }
        return versioncode + "";
    }

    public static String getAppVersionName(Context context) {
        String versionName = null;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
        } catch (Exception e) {
            //Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }

    public static String getAppName(Context context) {
        if (context == null) {
            return null;
        }
        try {
            PackageManager packageManager = context.getPackageManager();
            return String.valueOf(packageManager.getApplicationLabel(context.getApplicationInfo()));
        } catch (Throwable e) {
        }
        return null;
    }


}
