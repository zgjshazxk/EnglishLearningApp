package com.usts.englishlearning.database;

import org.litepal.crud.LitePalSupport;

public class DailyData extends LitePalSupport {

    private int id;

    private byte[] picVertical;

    private byte[] picHorizontal;

    private String dailyChs;

    private String dailyEn;

    private String dailySound;

    // 更新时间
    private String dayTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getPicVertical() {
        return picVertical;
    }

    public void setPicVertical(byte[] picVertical) {
        this.picVertical = picVertical;
    }

    public byte[] getPicHorizontal() {
        return picHorizontal;
    }

    public void setPicHorizontal(byte[] picHorizontal) {
        this.picHorizontal = picHorizontal;
    }

    public String getDailyChs() {
        return dailyChs;
    }

    public void setDailyChs(String dailyChs) {
        this.dailyChs = dailyChs;
    }

    public String getDailyEn() {
        return dailyEn;
    }

    public void setDailyEn(String dailyEn) {
        this.dailyEn = dailyEn;
    }

    public String getDayTime() {
        return dayTime;
    }

    public void setDayTime(String dayTime) {
        this.dayTime = dayTime;
    }

    public String getDailySound() {
        return dailySound;
    }

    public void setDailySound(String dailySound) {
        this.dailySound = dailySound;
    }
}
