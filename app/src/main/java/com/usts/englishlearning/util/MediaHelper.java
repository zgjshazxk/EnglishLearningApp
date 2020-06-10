package com.usts.englishlearning.util;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import com.usts.englishlearning.config.ConstantData;

public class MediaHelper {

    public static MediaPlayer mediaPlayer;

    // 英文发音
    public static final int ENGLISH_VOICE = 1;
    // 美国发音
    public static final int AMERICA_VOICE = 0;

    // 默认
    public static final int DEFAULT_VOICE = ENGLISH_VOICE;

    private static final String TAG = "MediaHelper";

    public static void play(int type, String wordName) {
        if (mediaPlayer != null) {
            releasePlayer();
            mediaPlayer = new MediaPlayer();
        } else
            mediaPlayer = new MediaPlayer();
        try {
            if (ENGLISH_VOICE == type)
                mediaPlayer.setDataSource(ConstantData.YOU_DAO_VOICE_EN + wordName);
            else
                mediaPlayer.setDataSource(ConstantData.YOU_DAO_VOICE_USA + wordName);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (mediaPlayer != null) {
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void play(String wordName) {
        play(DEFAULT_VOICE, wordName);
    }

    public static void playInternetSource(String address) {
        if (mediaPlayer != null) {
            releasePlayer();
            mediaPlayer = new MediaPlayer();
        } else
            mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(address);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (mediaPlayer != null) {
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void releasePlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public static void playLocalFile(int sourceAddress) {
        if (mediaPlayer != null) {
            releasePlayer();
            mediaPlayer = new MediaPlayer();
        } else
            mediaPlayer = new MediaPlayer();
        try {
            AssetFileDescriptor fileDescriptor = MyApplication.getContext().getResources().openRawResourceFd(sourceAddress);
            mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (mediaPlayer != null) {
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void playLocalFileRepeat(final int sourceAddress) {
        if (mediaPlayer != null) {
            releasePlayer();
            mediaPlayer = new MediaPlayer();
        } else
            mediaPlayer = new MediaPlayer();
        try {
            AssetFileDescriptor fileDescriptor = MyApplication.getContext().getResources().openRawResourceFd(sourceAddress);
            mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playLocalFileRepeat(sourceAddress);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
