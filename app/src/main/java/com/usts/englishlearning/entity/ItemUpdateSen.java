package com.usts.englishlearning.entity;

public class ItemUpdateSen {

    private String chsSentences;

    private String enSentences;

    public ItemUpdateSen(String chsSentences, String enSentences) {
        this.chsSentences = chsSentences;
        this.enSentences = enSentences;
    }

    public String getChsSentences() {
        return chsSentences;
    }

    public void setChsSentences(String chsSentences) {
        this.chsSentences = chsSentences;
    }

    public String getEnSentences() {
        return enSentences;
    }

    public void setEnSentences(String enSentences) {
        this.enSentences = enSentences;
    }
}
