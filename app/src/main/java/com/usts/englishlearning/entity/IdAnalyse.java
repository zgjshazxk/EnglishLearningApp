package com.usts.englishlearning.entity;

public class IdAnalyse {

    private int wordId;

    private int position;

    public IdAnalyse(int wordId, int position) {
        this.wordId = wordId;
        this.position = position;
    }

    public int getWordId() {
        return wordId;
    }

    public void setWordId(int wordId) {
        this.wordId = wordId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
