package com.usts.englishlearning.entity;

public class UpdateMeans {

    private String chsMeans;

    private String enMeans;

    public UpdateMeans() {
    }

    public UpdateMeans(String chsMeans, String enMeans) {
        this.chsMeans = chsMeans;
        this.enMeans = enMeans;
    }

    public String getChsMeans() {
        return chsMeans;
    }

    public void setChsMeans(String chsMeans) {
        this.chsMeans = chsMeans;
    }

    public String getEnMeans() {
        return enMeans;
    }

    public void setEnMeans(String enMeans) {
        this.enMeans = enMeans;
    }
}
