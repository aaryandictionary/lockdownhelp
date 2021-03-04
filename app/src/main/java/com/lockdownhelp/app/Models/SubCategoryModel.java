package com.lockdownhelp.app.Models;

public class SubCategoryModel {
    String subCategoryName,subCategoryIconUrl,subCategoryStatus,subCategoryUnit;
    double maxCounter,minCounter,multipleCounter;

    public double getMaxCounter() {
        return maxCounter;
    }

    public void setMaxCounter(double maxCounter) {
        this.maxCounter = maxCounter;
    }

    public double getMinCounter() {
        return minCounter;
    }

    public void setMinCounter(double minCounter) {
        this.minCounter = minCounter;
    }

    public double getMultipleCounter() {
        return multipleCounter;
    }

    public void setMultipleCounter(double multipleCounter) {
        this.multipleCounter = multipleCounter;
    }

    public SubCategoryModel() {
    }

    public String getSubCategoryName() {
        return subCategoryName;
    }

    public void setSubCategoryName(String subCategoryName) {
        this.subCategoryName = subCategoryName;
    }

    public String getSubCategoryIconUrl() {
        return subCategoryIconUrl;
    }

    public void setSubCategoryIconUrl(String subCategoryIconUrl) {
        this.subCategoryIconUrl = subCategoryIconUrl;
    }

    public String getSubCategoryStatus() {
        return subCategoryStatus;
    }

    public void setSubCategoryStatus(String subCategoryStatus) {
        this.subCategoryStatus = subCategoryStatus;
    }

    public String getSubCategoryUnit() {
        return subCategoryUnit;
    }

    public void setSubCategoryUnit(String subCategoryUnit) {
        this.subCategoryUnit = subCategoryUnit;
    }
}
