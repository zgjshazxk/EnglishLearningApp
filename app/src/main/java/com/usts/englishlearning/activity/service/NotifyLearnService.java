package com.usts.englishlearning.activity.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.usts.englishlearning.R;
import com.usts.englishlearning.activity.WelcomeActivity;
import com.usts.englishlearning.broadcast.AlarmReceiver;
import com.usts.englishlearning.broadcast.NotifyReceiver;
import com.usts.englishlearning.config.ConstantData;
import com.usts.englishlearning.database.Interpretation;
import com.usts.englishlearning.database.Word;
import com.usts.englishlearning.util.MediaHelper;
import com.usts.englishlearning.util.MyApplication;

import org.litepal.LitePal;
import org.litepal.util.Const;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotifyLearnService extends Service {

    public static int currentIndex = 0;

    public static final int ALL_MODE = 0;

    public static final int STAR_MODE = 1;

    public static final int LEARN_MODE = 2;

    public static final int RANDOM_MODE = 3;

    public static int currentMode = -1;

    private static final String TAG = "NotifyLearnService";

    public static List<Word> needWords;

    public NotifyLearnService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setWordsData();
        Notification notification = createNotification();
        updateNotification();
        startForeground(2, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static void updateNotification() {

        NotificationManager manager = (NotificationManager) MyApplication.getContext().getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 设置不一样的Id
            NotificationChannel notificationChannel = new NotificationChannel(ConstantData.channelId2, ConstantData.channelName2, NotificationManager.IMPORTANCE_LOW);
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);
            notificationChannel.setSound(null, null);
            notificationChannel.setVibrationPattern(new long[]{0});
            manager.createNotificationChannel(notificationChannel);
        }

        Notification notification = createNotification();

        manager.notify(2, notification);

    }

    public static void playSound() {
        MediaHelper.play(getCurrentWord().getWord());
    }

    public static void setWordsData() {
        needWords = new ArrayList<>();
        needWords.clear();
        if (currentMode == ALL_MODE)
            needWords = LitePal.select("wordId", "word").find(Word.class);
        else if (currentMode == LEARN_MODE)
            needWords = LitePal.where("isLearned = ?", 1 + "").select("wordId", "word").find(Word.class);
        else if (currentMode == STAR_MODE)
            needWords = LitePal.where("isCollected = ?", 1 + "").select("wordId", "word").find(Word.class);
        else if (currentMode == RANDOM_MODE) {
            needWords = LitePal.select("wordId", "word").find(Word.class);
            Collections.shuffle(needWords);
        }
        currentIndex = 0;
    }

    public static Word getCurrentWord() {

        if (currentIndex == needWords.size())
            currentIndex = 0;

        Word currentWord = needWords.get(currentIndex);
        return currentWord;
    }

    public static Notification createNotification() {
        Word currentWord = LitePal.where("wordId = ?", getCurrentWord().getWordId() + "").select("wordId", "word", "isCollected").find(Word.class).get(0);
        List<Interpretation> interpretationList = LitePal.where("wordId = ?", currentWord.getWordId() + "").find(Interpretation.class);
        StringBuilder stringBuilder = new StringBuilder();
        for (Interpretation interpretation : interpretationList) {
            stringBuilder.append(interpretation.getWordType() + ". " + interpretation.getCHSMeaning() + " ");
        }
        RemoteViews remoteViews = new RemoteViews(MyApplication.getContext().getPackageName(), R.layout.layout_notify);
        remoteViews.setTextViewText(R.id.text_notify_word_name, currentWord.getWord());
        remoteViews.setTextViewText(R.id.text_notify_word_mean, stringBuilder.toString());
        Intent intent = new Intent(MyApplication.getContext(), NotifyReceiver.class);
        intent.setAction(NotifyReceiver.UPDATE_ACTION);
        PendingIntent updatePendingIntent = PendingIntent.getBroadcast(MyApplication.getContext(), -1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent soundIntent = new Intent(MyApplication.getContext(), NotifyReceiver.class);
        soundIntent.setAction(NotifyReceiver.SOUND_ACTION);
        PendingIntent soundPendingIntent = PendingIntent.getBroadcast(MyApplication.getContext(), -1, soundIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (currentMode != STAR_MODE) {
            remoteViews.setViewVisibility(R.id.img_notify_star, View.VISIBLE);
            Intent starIntent = new Intent(MyApplication.getContext(), NotifyReceiver.class);
            starIntent.setAction(NotifyReceiver.STAR_ACTION);
            PendingIntent starPendingIntent = PendingIntent.getBroadcast(MyApplication.getContext(), -1, starIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.img_notify_star, starPendingIntent);
        } else {
            remoteViews.setViewVisibility(R.id.img_notify_star, View.GONE);
        }
        remoteViews.setOnClickPendingIntent(R.id.img_notify_next, updatePendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.img_notify_voice, soundPendingIntent);
        if (currentWord.getIsCollected() == 1) {
            remoteViews.setImageViewResource(R.id.img_notify_star, R.drawable.icon_star_fill);
            Log.d(TAG, "已收藏");
        } else {
            remoteViews.setImageViewResource(R.id.img_notify_star, R.drawable.icon_star_notify);
            Log.d(TAG, "未收藏");
        }
        Notification notification = new NotificationCompat.Builder(MyApplication.getContext(), ConstantData.channelId2)
                .setCustomBigContentView(remoteViews)
                .setCustomContentView(remoteViews)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.icon_tip)
                .setLargeIcon(BitmapFactory.decodeResource(MyApplication.getContext().getResources(), R.drawable.ic_launcher))
                .setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVibrate(new long[]{0})
                .setSound(null)
                .build();

        return notification;
    }

    public static void setStarStatus() {
        Word word = LitePal.where("wordId = ?", getCurrentWord().getWordId() + "").find(Word.class).get(0);
        Word newWord;
        if (word.getIsCollected() == 0) {
            Log.d(TAG, "setStarStatus: " + word.getWord());
            newWord = new Word();
            newWord.setIsCollected(1);
            newWord.updateAll("wordId = ?", word.getWordId() + "");
        } else {
            newWord = new Word();
            newWord.setToDefault("isCollected");
            newWord.updateAll("wordId = ?", word.getWordId() + "");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
