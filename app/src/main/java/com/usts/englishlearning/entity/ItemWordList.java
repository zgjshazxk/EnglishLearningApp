package com.usts.englishlearning.entity;

public class ItemWordList {

    private int wordId;

    private String wordName;

    private String wordMean;

    private boolean isOnClick;

    private boolean isSearch;

    public ItemWordList(int wordId, String wordName, String wordMean, boolean isOnClick, boolean isSearch) {
        this.wordId = wordId;
        this.wordName = wordName;
        this.wordMean = wordMean;
        this.isOnClick = isOnClick;
        this.isSearch = isSearch;
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

    public String getWordMean() {
        return wordMean;
    }

    public void setWordMean(String wordMean) {
        this.wordMean = wordMean;
    }

    public boolean isOnClick() {
        return isOnClick;
    }

    public void setOnClick(boolean onClick) {
        isOnClick = onClick;
    }

    public boolean isSearch() {
        return isSearch;
    }

    public void setSearch(boolean search) {
        isSearch = search;
    }
}
