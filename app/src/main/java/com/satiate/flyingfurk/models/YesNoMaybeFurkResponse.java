package com.satiate.flyingfurk.models;

/**
 * Created by Rishabh Bhatia on 29/10/16.
 */

public class YesNoMaybeFurkResponse {

    private String answer;
    private boolean forced;
    private String image;

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public boolean isForced() {
        return forced;
    }

    public void setForced(boolean forced) {
        this.forced = forced;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
