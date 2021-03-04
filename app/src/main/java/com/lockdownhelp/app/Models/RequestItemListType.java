package com.lockdownhelp.app.Models;

public class RequestItemListType {
    String itemName,itemUnit,itemId;
    double itemQuantity;

    public RequestItemListType() {
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemUnit() {
        return itemUnit;
    }

    public void setItemUnit(String itemUnit) {
        this.itemUnit = itemUnit;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public double getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(double itemQuantity) {
        this.itemQuantity = itemQuantity;
    }
}
