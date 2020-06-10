package com.usts.englishlearning.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.ActivityOptionsCompat;

import com.usts.englishlearning.activity.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class ActivityCollector {

    public static List<Activity> activities = new ArrayList<>();

    public static void addActivity(Activity activity) {
        activities.add(activity);
    }

    public static void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    public static void finishAll() {
        MainActivity.needRefresh = true;
        for (Activity activity : activities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }

    // 启动新的Activity
    /*
     * 注意：
     * Context中有一个startActivity方法，Activity继承自Context，重载了startActivity方法
     * 如果使用Activity的startActivity方法，不会有任何限制
     * 而如果使用Context的startActivity方法的話，就需要开启一个新的的task
     * 遇到这个异常，是因为使用了Context的startActivity方法。解决办法是，加一个flag
     *
     * */
    public static void startOtherActivity(Context context, Class cls) {
        Intent intent = new Intent();
        intent.setClass(MyApplication.getContext(), cls);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void startOtherActivity(Context context, Class cls, ActivityOptionsCompat activityOptionsCompat) {
        Intent intent = new Intent();
        intent.setClass(MyApplication.getContext(), cls);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent, activityOptionsCompat.toBundle());
    }

}
