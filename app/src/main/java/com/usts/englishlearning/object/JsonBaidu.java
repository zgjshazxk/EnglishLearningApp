package com.usts.englishlearning.object;

import java.util.List;

public class JsonBaidu {

    private String log_id;

    private List<JsonBaiduWords> words_result;

    public String getLog_id() {
        return log_id;
    }

    public void setLog_id(String log_id) {
        this.log_id = log_id;
    }

    public List<JsonBaiduWords> getWords_result() {
        return words_result;
    }

    public void setWords_result(List<JsonBaiduWords> words_result) {
        this.words_result = words_result;
    }

}
