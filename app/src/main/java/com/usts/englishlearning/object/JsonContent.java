package com.usts.englishlearning.object;

import java.util.List;

public class JsonContent {

    private String usphone;

    private String ukphone;

    private String picture;

    private JsonSentenceContent sentence;

    private JsonPhraseContent phrase;

    private JsonRemMethod remMethod;

    private List<JsonTran> trans;

    public String getUsphone() {
        return usphone;
    }

    public void setUsphone(String usphone) {
        this.usphone = usphone;
    }

    public String getUkphone() {
        return ukphone;
    }

    public void setUkphone(String ukphone) {
        this.ukphone = ukphone;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public JsonSentenceContent getSentence() {
        return sentence;
    }

    public void setSentence(JsonSentenceContent sentence) {
        this.sentence = sentence;
    }

    public JsonPhraseContent getPhrase() {
        return phrase;
    }

    public void setPhrase(JsonPhraseContent phrase) {
        this.phrase = phrase;
    }

    public JsonRemMethod getRemMethod() {
        return remMethod;
    }

    public void setRemMethod(JsonRemMethod remMethod) {
        this.remMethod = remMethod;
    }

    public List<JsonTran> getTrans() {
        return trans;
    }

    public void setTrans(List<JsonTran> trans) {
        this.trans = trans;
    }
}
