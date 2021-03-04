package com.lockdownhelp.app.Models;

public class Department {
    String categoryName,categoryIconUrl,categoryId,categoryCheck;


    public Department() {
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryIconUrl() {
        return categoryIconUrl;
    }

    public void setCategoryIconUrl(String categoryIconUrl) {
        this.categoryIconUrl = categoryIconUrl;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryCheck() {
        return categoryCheck;
    }

    public void setCategoryCheck(String categoryCheck) {
        this.categoryCheck = categoryCheck;
    }
}
