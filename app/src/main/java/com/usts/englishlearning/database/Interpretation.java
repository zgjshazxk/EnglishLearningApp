package com.usts.englishlearning.database;

import org.litepal.crud.LitePalSupport;

public class Interpretation extends LitePalSupport {

    // 词性
    private String wordType;

    // 中文词意
    private String CHSMeaning;

    // 英文词意
    private String ENMeaning;

    // 归属单词
    private int wordId;

    public String getWordType() {
        return wordType;
    }

    public void setWordType(String wordType) {
        this.wordType = wordType;
    }

    public String getCHSMeaning() {
        return CHSMeaning;
    }

    public void setCHSMeaning(String CHSMeaning) {
        this.CHSMeaning = CHSMeaning;
    }

    public String getENMeaning() {
        return ENMeaning;
    }

    public void setENMeaning(String ENMeaning) {
        this.ENMeaning = ENMeaning;
    }

    public int getWordId() {
        return wordId;
    }

    public void setWordId(int wordId) {
        this.wordId = wordId;
    }
}
