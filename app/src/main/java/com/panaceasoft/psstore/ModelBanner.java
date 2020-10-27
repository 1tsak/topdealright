package com.panaceasoft.psstore;

public class ModelBanner {
    private String url;
    private String banner_image;

    public String getUrl() {
        return url;
    }

    public String getBanner_image() {
        return banner_image;
    }

    public ModelBanner(String url, String banner_image) {
        this.url = url;
        this.banner_image = banner_image;
    }
}
