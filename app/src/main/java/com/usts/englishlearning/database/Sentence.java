package com.usts.englishlearning.database;

import org.litepal.crud.LitePalSupport;

public class Sentence extends LitePalSupport {

    // 英文句子
    private String enSentence;

    // 中文句子
    private String chsSentence;

    // 归属单词
    private int wordId;

    public int getWordId() {
        return wordId;
    }

    public void setWordId(int wordId) {
        this.wordId = wordId;
    }

    public String getEnSentence() {
        return enSentence;
    }

    public void setEnSentence(String enSentence) {
        this.enSentence = enSentence;
    }

    public String getChsSentence() {
        return chsSentence;
    }

    public void setChsSentence(String chsSentence) {
        this.chsSentence = chsSentence;
    }
}
