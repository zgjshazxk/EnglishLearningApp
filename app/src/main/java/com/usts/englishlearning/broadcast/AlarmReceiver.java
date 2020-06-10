package com.usts.englishlearning.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.usts.englishlearning.activity.AlarmActivity;
import com.usts.englishlearning.config.ConfigData;
import com.usts.englishlearning.worker.AlarmWorker;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive: " + action);

        WorkManager.getInstance().enqueue(new OneTimeWorkRequest.Builder(AlarmWorker.class).build());

        // 重复定时
        // 第二天的这个时间再提醒
        if (ConfigData.getIsAlarm()) {
            int hour = Integer.parseInt(ConfigData.getAlarmTime().split("-")[0]);
            int minute = Integer.parseInt(ConfigData.getAlarmTime().split("-")[1]);
            AlarmActivity.startAlarm(hour, minute, true, false);
        }

    }

}
