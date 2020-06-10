package com.usts.englishlearning.entity;

public class ItemMatch {

    private int id;

    private String wordString;

    private boolean isChosen;

    private boolean readyDelete;

    public ItemMatch(int id, String wordString, boolean isChosen, boolean readyDelete) {
        this.id = id;
        this.wordString = wordString;
        this.isChosen = isChosen;
        this.readyDelete = readyDelete;
    }

    public boolean isChosen() {
        return isChosen;
    }

    public void setChosen(boolean chosen) {
        isChosen = chosen;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWordString() {
        return wordString;
    }

    public void setWordString(String wordString) {
        this.wordString = wordString;
    }

    public boolean isReadyDelete() {
        return readyDelete;
    }

    public void setReadyDelete(boolean readyDelete) {
        this.readyDelete = readyDelete;
    }
}
