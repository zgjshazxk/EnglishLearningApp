package com.usts.englishlearning.entity;

public class ItemSearch {

    private int wordId;

    private String wordName;

    private String wordSound;

    private String wordMeans;

    public ItemSearch(int wordId, String wordName, String wordSound, String wordMeans) {
        this.wordId = wordId;
        this.wordName = wordName;
        this.wordSound = wordSound;
        this.wordMeans = wordMeans;
    }

    public int getWordId() {
        return wordId;
    }

    public void setWordId(int wordId) {
        this.wordId = wordId;
    }

    public String getWordName() {
        return wordName;
    }

    public void setWordName(String wordName) {
        this.wordName = wordName;
    }

    public String getWordSound() {
        return wordSound;
    }

    public void setWordSound(String wordSound) {
        this.wordSound = wordSound;
    }

    public String getWordMeans() {
        return wordMeans;
    }

    public void setWordMeans(String wordMeans) {
        this.wordMeans = wordMeans;
    }
}
