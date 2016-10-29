package com.satiate.flyingfurk.models;

/**
 * Created by Rishabh Bhatia on 29/10/16.
 */

public class Giphy {

    private GiphyData data;
    private GiphyMetadata meta;

    public GiphyData getData() {
        return data;
    }

    public void setData(GiphyData data) {
        this.data = data;
    }

    public GiphyMetadata getMeta() {
        return meta;
    }

    public void setMeta(GiphyMetadata meta) {
        this.meta = meta;
    }
}
