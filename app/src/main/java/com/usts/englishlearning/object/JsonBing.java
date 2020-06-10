package com.usts.englishlearning.object;

import java.util.List;

public class JsonBing {

    private List<JsonImg> images;

    private JsonTool tooltips;

    public List<JsonImg> getImages() {
        return images;
    }

    public void setImages(List<JsonImg> images) {
        this.images = images;
    }

    public JsonTool getTooltips() {
        return tooltips;
    }

    public void setTooltips(JsonTool tooltips) {
        this.tooltips = tooltips;
    }
}
