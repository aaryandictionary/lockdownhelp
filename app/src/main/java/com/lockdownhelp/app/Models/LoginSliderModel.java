package com.lockdownhelp.app.Models;

public class LoginSliderModel {
    String title;
    int imgUrl;

    public LoginSliderModel() {
    }

    public LoginSliderModel(String title, int imgUrl) {
        this.title = title;
        this.imgUrl = imgUrl;
    }

    public int getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(int imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
