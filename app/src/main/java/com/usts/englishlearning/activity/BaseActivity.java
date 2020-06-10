package com.usts.englishlearning.activity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.gyf.immersionbar.ImmersionBar;
import com.usts.englishlearning.config.ConfigData;
import com.usts.englishlearning.config.ConstantData;
import com.usts.englishlearning.database.DailyData;
import com.usts.englishlearning.listener.PermissionListener;
import com.usts.englishlearning.object.JsonBing;
import com.usts.englishlearning.object.JsonDailySentence;
import com.usts.englishlearning.util.ActivityCollector;
import com.usts.englishlearning.util.HttpHelper;
import com.usts.englishlearning.util.TimeController;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    private PermissionListener mListener;

    private static final int PERMISSION_REQUESTCODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, getClass().getSimpleName());

        if (ConfigData.getIsNight()) {
            // 沉浸式状态栏，设置深色
            ImmersionBar.with(this)
                    .statusBarDarkFont(false)
                    .init();
        } else {
            ImmersionBar.with(this)
                    .statusBarDarkFont(true)
                    .init();
        }

        ActivityCollector.addActivity(this);

        // 防止输入法将布局顶上去
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    // 权限
    public void requestRunPermission(String[] permissions, PermissionListener listener) {
        mListener = listener;
        List<String> permissionLists = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionLists.add(permission);
            }
        }
        if (!permissionLists.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionLists.toArray(new String[permissionLists.size()]), PERMISSION_REQUESTCODE);
        } else {
            //表示全都授权了
            mListener.onGranted();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUESTCODE:
                if (grantResults.length > 0) {
                    // 存放没授权的权限
                    List<String> deniedPermissions = new ArrayList<>();
                    for (int i = 0; i < grantResults.length; i++) {
                        int grantResult = grantResults[i];
                        String permission = permissions[i];
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            deniedPermissions.add(permission);
                        }
                    }
                    if (deniedPermissions.isEmpty()) {
                        // 说明都授权了
                        mListener.onGranted();
                    } else {
                        mListener.onDenied(deniedPermissions);
                    }
                }
                break;
            default:
                break;
        }
    }

    public static boolean isServiceExisted(Context context, String className) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                .getRunningServices(Integer.MAX_VALUE);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            ActivityManager.RunningServiceInfo serviceInfo = serviceList.get(i);
            ComponentName serviceName = serviceInfo.service;
            if (serviceName.getClassName().equals(className)) {
                return true;
            }
        }
        return false;
    }

    // 开启数据监测
    public static void prepareDailyData() {
        long currentDate = TimeController.getCurrentDateStamp();
        List<DailyData> dailyDataList = LitePal.where("dayTime = ?", currentDate + "").find(DailyData.class);

        if (dailyDataList.isEmpty()) {
            analyseJsonAndSave();
        } else {
            if (dailyDataList.get(0).getPicVertical() == null ||
                    dailyDataList.get(0).getPicHorizontal() == null ||
                    dailyDataList.get(0).getDailyEn() == null ||
                    dailyDataList.get(0).getDailyChs() == null) {
                analyseJsonAndSave();
            }
        }
    }

    public static void analyseJsonAndSave() {
        byte[] imgVertical;
        byte[] imgHorizontal;
        String dailyCn;
        String dailyEn;
        String result = "", json, tem;
        LitePal.deleteAll(DailyData.class);
        DailyData dailyData = new DailyData();
        try {
            json = HttpHelper.requestResult(ConstantData.IMG_API);
            Log.d(TAG, "数据" + json);
            Gson gson = new Gson();
            JsonBing jsonBing = gson.fromJson(json, JsonBing.class);
            Log.d(TAG, "prepareDailyData: " + jsonBing.toString());
            tem = ConstantData.IMG_API_BEFORE + jsonBing.getImages().get(0).getUrl();
            Log.d(TAG, "URL" + tem);
            imgHorizontal = HttpHelper.requestBytes(tem);
            if (tem.indexOf("1920x1080") != -1) {
                result = tem.replace("1920x1080", "1080x1920");
            } else {
                result = tem;
            }
            imgVertical = HttpHelper.requestBytes(result);
            json = HttpHelper.requestResult(ConstantData.DAILY_SENTENCE_API);
            Gson gson2 = new Gson();
            JsonDailySentence dailySentence = gson2.fromJson(json, JsonDailySentence.class);
            dailyCn = dailySentence.getNote();
            dailyEn = dailySentence.getContent();
            dailyData.setPicHorizontal(imgHorizontal);
            dailyData.setPicVertical(imgVertical);
            dailyData.setDailyEn(dailyEn);
            dailyData.setDailyChs(dailyCn);
            dailyData.setDailySound(dailySentence.getTts());
            dailyData.setDayTime(TimeController.getCurrentDateStamp() + "");
            dailyData.save();
        } catch (Exception e) {
            Log.d(TAG, "prepareDailyData: " + e.toString());
        }
    }

    public void windowFade() {
        getWindow().setEnterTransition(new Fade().setDuration(500));
        getWindow().setExitTransition(new Fade().setDuration(500));
        getWindow().setReenterTransition(new Fade().setDuration(500));
        getWindow().setReturnTransition(new Fade().setDuration(500));
    }

    public void windowSlide(int position) {
        getWindow().setEnterTransition(new Slide(position).setDuration(300));
        getWindow().setExitTransition(new Slide(position).setDuration(300));
        getWindow().setReenterTransition(new Slide(position).setDuration(300));
        getWindow().setReturnTransition(new Slide(position).setDuration(300));
    }

    public void windowExplode() {
        getWindow().setEnterTransition(new Explode().setDuration(300));
        getWindow().setExitTransition(new Explode().setDuration(300));
        getWindow().setReenterTransition(new Explode().setDuration(300));
        getWindow().setReturnTransition(new Explode().setDuration(300));
    }

    // 不支持夜间模式
    public void noNight(){
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    /*
     * fromXType：动画开始前的X坐标类型。取值范围为ABSOLUTE（绝对位置）、RELATIVE_TO_SELF（以自身宽或高为参考）、RELATIVE_TO_PARENT（以父控件宽或高为参考）。
     * fromXValue：动画开始前的X坐标值。当对应的Type为ABSOLUTE时，表示绝对位置；否则表示相对位置，1.0表示100%。
     * toXType：动画结束后的X坐标类型。
     * toXValue：动画结束后的X坐标值。
     * fromYType：动画开始前的Y坐标类型。
     * fromYValue：动画开始前的Y坐标值。
     * toYType：动画结束后的Y坐标类型。
     * toYValue：动画结束后的Y坐标值
     * *//*
    // 下部分操作布局从底部进入动画
    animation = new TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, 0.0f,Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 1.0f,Animation.RELATIVE_TO_PARENT, 0.0f
    );
            animation.setDuration(2000);
    //relativeLayout.startAnimation(animation);
    //imgShow.setVisibility(View.VISIBLE);
    // 上部分从顶部进入动画
    animation = new TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, -0.5f, Animation.RELATIVE_TO_PARENT, 0.0f
    );
            animation.setDuration(2000);
    // imgShow.startAnimation(animation);*/

}
