package com.usts.englishlearning.entity;

public class GameWord {

    private int id;

    private String wordName;

    private String wordMeans;

    public GameWord(int id, String wordName, String wordMeans) {
        this.id = id;
        this.wordName = wordName;
        this.wordMeans = wordMeans;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWordName() {
        return wordName;
    }

    public void setWordName(String wordName) {
        this.wordName = wordName;
    }

    public String getWordMeans() {
        return wordMeans;
    }

    public void setWordMeans(String wordMeans) {
        this.wordMeans = wordMeans;
    }
}
