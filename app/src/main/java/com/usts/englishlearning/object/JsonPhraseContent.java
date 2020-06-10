package com.usts.englishlearning.object;

import java.util.List;

public class JsonPhraseContent {

    private List<JsonPhrase> phrases;

    private String desc;

    public List<JsonPhrase> getPhrases() {
        return phrases;
    }

    public void setPhrases(List<JsonPhrase> phrases) {
        this.phrases = phrases;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
