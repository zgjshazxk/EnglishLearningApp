package com.usts.englishlearning.entity;

public class ItemWordMeanChoice {

    public static final int NOTSTART = -1;
    public static final int RIGHT = 0;
    public static final int WRONG = 1;

    // ID值，用来与正确的值进行判断
    private int id;

    // 存放内容
    private String wordMean;

    // 存放判断状态
    // -1代表未选，0代表正确，1代表错误
    private int isRight;

    public ItemWordMeanChoice(int id, String wordMean, int isRight) {
        this.id = id;
        this.wordMean = wordMean;
        this.isRight = isRight;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWordMean() {
        return wordMean;
    }

    public void setWordMean(String wordMean) {
        this.wordMean = wordMean;
    }

    public int isRight() {
        return isRight;
    }

    public void setRight(int right) {
        isRight = right;
    }
}
